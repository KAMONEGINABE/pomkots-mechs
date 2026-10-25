package grcmcs.minecraft.mods.pomkotsmechs.config.datapack;

import com.google.common.reflect.TypeToken;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.raid.EventDefinition;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.raid.RaidDefinition;
import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.raid.WaveDefinition;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.CircuitSkill;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.data.CircuitSkillJsonParser;
import grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.registry.CircuitRegistries;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.datafix.fixes.BlockEntitySignTextStrictJsonFix;

public class PomkotsDataPackManager {
   private static final PomkotsDataPackManager singleton = new PomkotsDataPackManager();
   private final PomkotsDataPack dataPackServer = new PomkotsDataPack();
   private PomkotsDataPack dataPackClient = new PomkotsDataPack();

   public static PomkotsDataPackManager getInstance() {
      return singleton;
   }

   public PomkotsDataPack getDataPack() {
      return Platform.getEnvironment() == Env.SERVER ? this.dataPackServer : this.dataPackClient;
   }

   public void loadDataPack(ResourceManager manager) {
      this.dataPackServer.reset();
      this.loadAllPartsData(manager);
      this.loadAllEnemyData(manager);
      this.loadAllRaidData(manager);
      this.loadAllChestData(manager);
      this.loadAllTraderData(manager);
      this.loadAllFighterData(manager);
      this.loadAllAssetsData(manager);
      this.loadAllSkillData(manager);
      if (Platform.getEnvironment() == Env.CLIENT && !this.dataPackServer.isEmpty()) {
         this.dataPackClient = this.dataPackServer;
      }

      PomkotsMechs.LOGGER.info("Data Pack loaded");
   }

   private void loadResources(ResourceLocation path, ResourceManager manager, Consumer<Resource> handler) {
      List<Resource> resources;
      try {
         resources = manager.getResourceStack(path);
      } catch (Exception e) {
         PomkotsMechs.LOGGER.error("Failed to load resource stack: {}", path, e);
         return;
      }

      resources.sort((a, b) -> {
         String idA = a.source().packId();
         String idB = b.source().packId();
         int prioA = this.getPriority(idA);
         int prioB = this.getPriority(idB);
         return Integer.compare(prioA, prioB);
      });

      for (Resource resource : resources) {
         try {
            handler.accept(resource);
         } catch (Exception e) {
            PomkotsMechs.LOGGER.error("Failed to handle resource: {}", resource, e);
         }
      }
   }

   private int getPriority(String packId) {
      if (packId.equals("pomkotsmechs")) {
         return 0;
      } else {
         return packId.contains("pomkotsmechs") ? 1 : 2;
      }
   }

   private void loadAllPartsData(ResourceManager manager) {
      ResourceLocation path = new ResourceLocation("pomkotsmechs", "parts.json");
      this.loadResources(path, manager, resource -> {
         try (InputStream stream = resource.open()) {
            JsonObject json = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
            if (json != null) {
               this.loadRootPartsData(json);
            } else {
               PomkotsMechs.LOGGER.error("No parts on a parts pack: {}", resource);
            }
         } catch (IOException e) {
            PomkotsMechs.LOGGER.error("Failed to load a parts pack: {}", resource, e);
         }
      });
   }

   private void loadRootPartsData(JsonObject json) {
      JsonObject root = json.get("mech_parts").getAsJsonObject();
      if (root != null) {
         for (Entry<String, JsonElement> item : root.asMap().entrySet()) {
            this.loadPartsData(item.getValue(), item.getKey());
         }
      }
   }

   private void loadPartsData(JsonElement itemEle, String itemName) {
      JsonElement typeEle = itemEle.getAsJsonObject().get("type");
      if (typeEle != null && "parts".equals(typeEle.getAsString())) {
         JsonObject obj = itemEle.getAsJsonObject();
         this.registerPartsDataSingle(itemName + "head", obj.get("head"));
         this.registerPartsDataSingle(itemName + "body", obj.get("body"));
         this.registerPartsDataSingle(itemName + "arm", obj.get("arm"));
         this.registerPartsDataSingle(itemName + "legs", obj.get("legs"));
      } else {
         this.registerPartsDataSingle(itemName, itemEle);
      }
   }

   private void registerPartsDataSingle(String itemName, JsonElement itemRootEle) {
      if (itemRootEle != null) {
         JsonObject itemRoot = itemRootEle.getAsJsonObject();
         PomkotsDataPack.PartsData data = new PomkotsDataPack.PartsData();
         data.id = itemName;
         data.weight = this.getInt(itemRoot, "weight");
         data.description = this.getString(itemRoot, "description");
         JsonElement levelArrayEle = itemRoot.get("level_param");
         if (levelArrayEle != null) {
            JsonArray levelArray = levelArrayEle.getAsJsonArray();

            for (JsonElement levelParamsEle : levelArray.asList()) {
               JsonObject levelParamsObj = levelParamsEle.getAsJsonObject();
               PomkotsDataPack.LevelData levelData = new PomkotsDataPack.LevelData();
               levelData.durability = this.getInt(levelParamsObj, "durability");
               levelData.maxWeight = this.getInt(levelParamsObj, "max_weight");
               levelData.speedModifier = this.getFloat(levelParamsObj, "speed_modifier");
               levelData.jumpModifier = this.getFloat(levelParamsObj, "jump_modifier");
               levelData.damage = this.getFloat(levelParamsObj, "damage");
               levelData.missileMaxNum = this.getInt(levelParamsObj, "missile_max_num");
               levelData.missileLockInterval = this.getFloat(levelParamsObj, "missile_lock_interval");
               levelData.maxEnergy = this.getInt(levelParamsObj, "max_energy");
               levelData.energyChargePerTick = this.getFloat(levelParamsObj, "energy_charge_per_tick");
               levelData.workSecPerFuel = this.getInt(levelParamsObj, "work_sec_per_fuel");
               levelData.energyConsumeEvasion = this.getInt(levelParamsObj, "energy_consume_evasion");
               levelData.energyConsumeVertical = this.getInt(levelParamsObj, "energy_consume_vertical");
               levelData.speedModifierEvasion = this.getFloat(levelParamsObj, "speed_modifier_evasion");
               levelData.speedModifierVertical = this.getFloat(levelParamsObj, "speed_modifier_vertical");
               levelData.bulletsPerMagazine = this.getInt(levelParamsObj, "bullets_per_magazine");
               levelData.energy = this.getInt(levelParamsObj, "energy");
               levelData.energyConsumePerTick = this.getInt(levelParamsObj, "energy_consume_per_tick");
               data.levels.add(levelData);
            }
         }

         JsonElement recipeArrayEle = itemRoot.get("recipes");
         if (recipeArrayEle != null) {
            JsonArray recipeArray = recipeArrayEle.getAsJsonArray();

            for (JsonElement recipeEle : recipeArray.asList()) {
               Map<String, JsonElement> recipe = recipeEle.getAsJsonObject().asMap();
               List<PomkotsDataPack.SerializablePair<String, Integer>> rec = new ArrayList<>();

               for (Entry<String, JsonElement> material : recipe.entrySet()) {
                  rec.add(new PomkotsDataPack.SerializablePair<>(material.getKey(), material.getValue().getAsInt()));
               }

               data.recipes.add(rec);
            }
         }

         this.dataPackServer.addPartsData(itemName, data);
      }
   }

   private void loadAllEnemyData(ResourceManager manager) {
      ResourceLocation path = new ResourceLocation("pomkotsmechs", "enemies.json");
      this.loadResources(path, manager, resource -> {
         try (InputStream stream = resource.open()) {
            JsonObject json = JsonParser.parseReader(new InputStreamReader(stream)).getAsJsonObject();
            if (json != null) {
               this.loadRootEnemyData(json);
            } else {
               PomkotsMechs.LOGGER.error("No enemy on a enemies pack:" + resource);
            }
         } catch (IOException e) {
            PomkotsMechs.LOGGER.error("Failed to load a enemies pack:" + resource, e);
         }
      });
   }

   private void loadRootEnemyData(JsonObject json) {
      JsonObject root = json.get("enemy_data").getAsJsonObject();
      if (root != null) {
         for (Entry<String, JsonElement> enemy : root.asMap().entrySet()) {
            this.loadEnemyData(enemy.getValue(), enemy.getKey());
         }
      }
   }

   private void loadEnemyData(JsonElement enemyEle, String enemyName) {
      PomkotsDataPack.EnemyData data = new PomkotsDataPack.EnemyData();
      data.id = enemyName;
      data.speed = this.getFloat(enemyEle, "speed");
      data.maxStepUp = this.getFloat(enemyEle, "max_step_up");
      data.knockBackResistance = this.getFloat(enemyEle, "knock_back_resistance");
      data.followRange = this.getFloat(enemyEle, "follow_range");
      data.health = this.getInt(enemyEle, "health");
      data.baseDamageModifier = this.getFloat(enemyEle, "base_damage_modifier");
      data.explosionDamageModifier = this.getFloat(enemyEle, "explosion_damage_modifier");
      data.armor = this.getFloat(enemyEle, "armor");
      data.armorToughness = this.getFloat(enemyEle, "armor_toughness");
      data.bulletDamage = this.getFloat(enemyEle, "bullet_damage");
      data.bulletSpeed = this.getFloat(enemyEle, "bullet_speed");
      data.missileDamage = this.getFloat(enemyEle, "missile_damage");
      data.missileSpeed = this.getFloat(enemyEle, "missile_speed");
      data.grenadeDamage = this.getFloat(enemyEle, "grenade_damage");
      data.grenadeSpeed = this.getFloat(enemyEle, "grenade_speed");
      data.grenadeExplosionScale = this.getFloat(enemyEle, "grenade_explosion_scale");
      data.meleeDamage = this.getFloat(enemyEle, "melee_damage");
      data.meleeSpeed = this.getFloat(enemyEle, "melee_speed");
      data.laserDamage = this.getFloat(enemyEle, "laser_damage");
      data.laserSpeed = this.getFloat(enemyEle, "laser_speed");
      data.exAttack1Damage = this.getFloat(enemyEle, "ex_attack_1_damage");
      data.exAttack1Speed = this.getFloat(enemyEle, "ex_attack_1_speed");
      data.exAttack2Damage = this.getFloat(enemyEle, "ex_attack_2_damage");
      data.exAttack2Speed = this.getFloat(enemyEle, "ex_attack_2_speed");
      data.exAttack3Damage = this.getFloat(enemyEle, "ex_attack_3_damage");
      data.exAttack3Speed = this.getFloat(enemyEle, "ex_attack_3_speed");
      this.dataPackServer.addEnemyData(enemyName, data);
   }

   private int getInt(JsonElement parent, String name) {
      if (parent == null) {
         return 0;
      }

      JsonElement el = parent.getAsJsonObject().get(name);
      return el == null ? 0 : el.getAsInt();
   }

   private float getFloat(JsonElement parent, String name) {
      if (parent == null) {
         return 0.0F;
      }

      JsonElement el = parent.getAsJsonObject().get(name);
      return el == null ? 0.0F : el.getAsFloat();
   }

   private String getString(JsonElement parent, String name) {
      if (parent == null) {
         return "";
      }

      JsonElement el = parent.getAsJsonObject().get(name);
      return el == null ? "" : el.getAsString();
   }

   private void loadAllRaidData(ResourceManager manager) {
      ResourceLocation path = new ResourceLocation("pomkotsmechs", "raid.json");
      this.loadResources(
         path,
         manager,
         resource -> {
            PomkotsMechs.LOGGER.info("Resource Pack:" + resource.sourcePackId() + ":" + resource);

            try (InputStream stream = resource.open()) {
               Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
               Map<String, RaidDefinition> allRd = (Map<String, RaidDefinition>)BlockEntitySignTextStrictJsonFix.GSON
                  .fromJson(reader, (new TypeToken<Map<String, RaidDefinition>>() {}).getType());

               for (Entry<String, RaidDefinition> entry : allRd.entrySet()) {
                  if (entry.getKey() != null && entry.getValue() != null) {
                     RaidDefinition rd = entry.getValue();
                     if (rd.waves != null) {
                        for (WaveDefinition wd : rd.waves) {
                           if (wd.events != null) {
                              for (EventDefinition ed : wd.events) {
                                 if ("time".equals(ed.trigger_type)) {
                                    if (wd.timeline == null) {
                                       wd.timeline = new HashMap<>();
                                    }

                                    wd.timeline.put(ed.trigger_tick, ed);
                                 }
                              }
                           }
                        }
                     }

                     this.dataPackServer.addRaidData(entry.getKey(), rd);
                  }
               }
            } catch (IOException e) {
               PomkotsMechs.LOGGER.error("Failed to load a raid pack:" + resource, e);
            }
         }
      );
   }

   private void loadAllChestData(ResourceManager manager) {
      ResourceLocation path = new ResourceLocation("pomkotsmechs", "chest.json");
      this.loadResources(
         path,
         manager,
         resource -> {
            try (InputStream stream = resource.open()) {
               Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
               Map<String, PomkotsDataPack.ChestData> allRd = (Map<String, PomkotsDataPack.ChestData>)BlockEntitySignTextStrictJsonFix.GSON
                  .fromJson(reader, (new TypeToken<Map<String, PomkotsDataPack.ChestData>>() {}).getType());

               for (Entry<String, PomkotsDataPack.ChestData> entry : allRd.entrySet()) {
                  if (entry.getKey() != null && entry.getValue() != null) {
                     this.dataPackServer.addChestData(entry.getKey(), entry.getValue());
                  }
               }
            } catch (IOException e) {
               PomkotsMechs.LOGGER.error("Failed to load a raid pack:" + resource, e);
            }
         }
      );
   }

   private void loadAllTraderData(ResourceManager manager) {
      ResourceLocation path = new ResourceLocation("pomkotsmechs", "trader_pool.json");
      this.loadResources(
         path,
         manager,
         resource -> {
            try (InputStream stream = resource.open()) {
               Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
               Map<String, List<PomkotsDataPack.TraderPoolItem>> allRd = (Map<String, List<PomkotsDataPack.TraderPoolItem>>)BlockEntitySignTextStrictJsonFix.GSON
                  .fromJson(reader, (new TypeToken<Map<String, List<PomkotsDataPack.TraderPoolItem>>>() {}).getType());

               for (Entry<String, List<PomkotsDataPack.TraderPoolItem>> entry : allRd.entrySet()) {
                  if (entry.getKey() != null && entry.getValue() != null) {
                     this.dataPackServer.setTraderPool(entry.getValue());
                  }
               }
            } catch (IOException e) {
               PomkotsMechs.LOGGER.error("Failed to load a trader pack:" + resource, e);
            }
         }
      );
   }

   private void loadAllFighterData(ResourceManager manager) {
      ResourceLocation path = new ResourceLocation("pomkotsmechs", "arena_fighter_pool.json");
      this.loadResources(
         path,
         manager,
         resource -> {
            try (InputStream stream = resource.open()) {
               Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
               Map<String, List<PomkotsDataPack.FighterJson>> allRd = (Map<String, List<PomkotsDataPack.FighterJson>>)BlockEntitySignTextStrictJsonFix.GSON
                  .fromJson(reader, (new TypeToken<Map<String, List<PomkotsDataPack.FighterJson>>>() {}).getType());

               for (Entry<String, List<PomkotsDataPack.FighterJson>> entry : allRd.entrySet()) {
                  if (entry.getKey() != null && entry.getValue() != null) {
                     this.dataPackServer.setFighterPool(entry.getValue());
                  }
               }
            } catch (IOException e) {
               PomkotsMechs.LOGGER.error("Failed to load a trader pack:" + resource, e);
            }
         }
      );
   }

   private void loadAllAssetsData(ResourceManager manager) {
      ResourceLocation path = new ResourceLocation("pomkotsmechs", "assets.json");
      this.loadResources(
         path,
         manager,
         resource -> {
            try (InputStream stream = resource.open()) {
               Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
               Map<String, List<PomkotsDataPack.AssetDefinition>> all = (Map<String, List<PomkotsDataPack.AssetDefinition>>)BlockEntitySignTextStrictJsonFix.GSON
                  .fromJson(reader, (new TypeToken<Map<String, List<PomkotsDataPack.AssetDefinition>>>() {}).getType());

               for (Entry<String, List<PomkotsDataPack.AssetDefinition>> entry : all.entrySet()) {
                  if (entry.getKey() != null && entry.getValue() != null) {
                     this.dataPackServer.setAssetDefinitions(entry.getValue());
                  }
               }
            } catch (IOException e) {
               PomkotsMechs.LOGGER.error("Failed to load a trader pack:" + resource, e);
            }
         }
      );
   }

   private void loadAllSkillData(ResourceManager manager) {
      ResourceLocation path = new ResourceLocation("pomkotsmechs", "circuit_skills.json");
      this.loadResources(path, manager, resource -> {
         List<CircuitSkill> skills = new ArrayList<>();

         try (InputStream stream = resource.open()) {
            Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            JsonObject root = (JsonObject)GsonHelper.fromJson(BlockEntitySignTextStrictJsonFix.GSON, reader, JsonObject.class);
            if (root == null) {
               throw new IllegalArgumentException("Root JSON object is null.");
            }

            for (JsonElement element : GsonHelper.getAsJsonArray(root, "skills")) {
               if (!element.isJsonObject()) {
                  throw new IllegalArgumentException("Skill entry must be object in file: circuit_skills.json");
               }

               CircuitSkill skill = CircuitSkillJsonParser.parse(element.getAsJsonObject());
               skills.add(skill);
            }
         } catch (IOException e) {
            PomkotsMechs.LOGGER.error("Failed to load a trader pack:" + resource, e);
         }

         this.applySkills(skills);
      });
   }

   private void applySkills(List<CircuitSkill> skills) {
      CircuitRegistries.SKILLS.unfreezeForReload();
      CircuitRegistries.SKILLS.clearForReload();
      int loaded = 0;
      int failed = 0;

      for (CircuitSkill skill : skills) {
         try {
            CircuitRegistries.SKILLS.register(skill);
            loaded++;
         } catch (Exception e) {
            failed++;
            PomkotsMechs.LOGGER.error("Failed to register circuit skill '{}'", skill.id(), e);
         }
      }

      PomkotsMechs.LOGGER.info("Loaded {} circuit skills. Failed registrations: {}", loaded, failed);
   }

   public void serializeServerData(OutputStream outputStream) throws IOException {
      ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
      objectOutputStream.writeObject(this.dataPackServer);
      objectOutputStream.flush();
      objectOutputStream.close();
   }

   public PomkotsDataPack deSerialize(InputStream inputStream) throws IOException, ClassNotFoundException {
      ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
      this.dataPackClient = (PomkotsDataPack)objectInputStream.readObject();
      objectInputStream.close();
      return this.dataPackClient;
   }
}
