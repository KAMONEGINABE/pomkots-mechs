package grcmcs.minecraft.mods.pomkotsmechs.config.datapack;

import grcmcs.minecraft.mods.pomkotsmechs.config.datapack.raid.RaidDefinition;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PomkotsDataPack implements Serializable {
   private final Map<String, PomkotsDataPack.PartsData> partsData = new HashMap<>();
   private final Map<String, PomkotsDataPack.EnemyData> enemyData = new HashMap<>();
   private final Map<String, RaidDefinition> raidData = new HashMap<>();
   private final Map<String, PomkotsDataPack.ChestData> chestData = new HashMap<>();
   private final Map<String, List<PomkotsDataPack.TraderPoolItem>> traderData = new HashMap<>();
   private final Map<String, List<PomkotsDataPack.FighterJson>> fighterData = new HashMap<>();
   private final Map<String, PomkotsDataPack.AssetDefinition> assetDefinitions = new HashMap<>();

   public PomkotsDataPack.PartsData getPartsData(String partsName) {
      return this.partsData.get(partsName);
   }

   public void addPartsData(String partsName, PomkotsDataPack.PartsData data) {
      this.partsData.put(partsName, data);
   }

   public void reset() {
      this.partsData.clear();
      this.enemyData.clear();
      this.raidData.clear();
      this.chestData.clear();
      this.traderData.clear();
      this.fighterData.clear();
      this.assetDefinitions.clear();
   }

   public boolean isEmpty() {
      return this.partsData.isEmpty();
   }

   @Override
   public String toString() {
      return "{" + this.partsData.toString() + "," + this.enemyData.toString() + "," + this.raidData.toString() + "}";
   }

   public PomkotsDataPack.EnemyData getEnemyData(String enemyName) {
      return this.enemyData.get(enemyName);
   }

   public void addEnemyData(String enemyName, PomkotsDataPack.EnemyData data) {
      this.enemyData.put(enemyName, data);
   }

   public RaidDefinition getRaidData(String raidName) {
      return this.raidData.get(raidName);
   }

   public void addRaidData(String raidName, RaidDefinition data) {
      this.raidData.put(raidName, data);
   }

   public PomkotsDataPack.ChestData getChestData(String chestLootTable) {
      return this.chestData.get(chestLootTable);
   }

   public void addChestData(String chestLootTable, PomkotsDataPack.ChestData data) {
      this.chestData.put(chestLootTable, data);
   }

   public List<PomkotsDataPack.TraderPoolItem> getTraderPoolItems() {
      return this.traderData.get("entries");
   }

   public void setTraderPool(List<PomkotsDataPack.TraderPoolItem> pool) {
      this.traderData.put("entries", pool);
   }

   public List<PomkotsDataPack.FighterJson> getFighterPoolItems() {
      return this.fighterData.get("entries");
   }

   public void setFighterPool(List<PomkotsDataPack.FighterJson> pool) {
      this.fighterData.put("entries", pool);
   }

   public Map<String, PomkotsDataPack.AssetDefinition> getAssetDefinitions() {
      return this.assetDefinitions;
   }

   public void setAssetDefinitions(List<PomkotsDataPack.AssetDefinition> list) {
      this.assetDefinitions.clear();

      for (PomkotsDataPack.AssetDefinition entry : list) {
         this.assetDefinitions.put(entry.asset_id, entry);
      }
   }

   public class AssetDefinition implements Serializable {
      private String asset_id;
      private String asset_type;
      private int latest_version;
      private String npc_id;
      private String structure_id;
      private Map<Integer, String> functions;

      public String getAssetId() {
         return this.asset_id;
      }

      public String getAssetType() {
         return this.asset_type;
      }

      public int getLatestVersion() {
         return this.latest_version;
      }

      public Map<Integer, String> getFunctions() {
         return this.functions;
      }

      public String getNpcId() {
         return this.npc_id;
      }

      public String getStructureId() {
         return this.structure_id;
      }
   }

   public class AssetDefinitionsRoot {
      private List<PomkotsDataPack.AssetDefinition> assets;

      public List<PomkotsDataPack.AssetDefinition> getAssets() {
         return this.assets;
      }
   }

   public static class ChestData implements Serializable {
      public String key = "";
      public String type = "";
      public String raid_id;
   }

   public static class ChestMobSpawnData implements Serializable {
      public String type = "";
   }

   public static class EnemyData implements Serializable {
      public String id;
      public float followRange;
      public float speed;
      public float maxStepUp;
      public float knockBackResistance;
      public float health;
      public float baseDamageModifier;
      public float explosionDamageModifier;
      public float armor;
      public float armorToughness;
      public float bulletDamage;
      public float bulletSpeed;
      public float missileDamage;
      public float missileSpeed;
      public float grenadeDamage;
      public float grenadeSpeed;
      public float grenadeExplosionScale;
      public float meleeDamage;
      public float meleeSpeed;
      public float laserDamage;
      public float laserSpeed;
      public float exAttack1Damage;
      public float exAttack1Speed;
      public float exAttack2Damage;
      public float exAttack2Speed;
      public float exAttack3Damage;
      public float exAttack3Speed;

      @Override
      public String toString() {
         return "{" + this.id + "," + this.health + "," + this.armor + "}";
      }
   }

   public static class FighterJson implements Serializable {
      public String name;
      public String comment;
      public String ai_level;
      public int initial_rate;
      public int win;
      public int lose;
      public String model;
      public String texture;
      public PomkotsDataPack.MechJson mech;
   }

   public static class LevelData implements Serializable {
      public int durability;
      public int maxWeight;
      public float speedModifier;
      public float jumpModifier;
      public float damage;
      public int missileMaxNum;
      public float missileLockInterval;
      public int maxEnergy;
      public float energyChargePerTick;
      public int workSecPerFuel;
      public int energyConsumeEvasion;
      public int energyConsumeVertical;
      public float speedModifierEvasion;
      public float speedModifierVertical;
      public int bulletsPerMagazine;
      public int energy;
      public int energyConsumePerTick;

      @Override
      public String toString() {
         return "["
            + this.durability
            + ","
            + this.maxWeight
            + ","
            + this.speedModifier
            + ","
            + this.jumpModifier
            + ","
            + this.damage
            + ","
            + this.missileMaxNum
            + ","
            + this.missileLockInterval
            + ","
            + this.energyConsumePerTick
            + "]";
      }
   }

   public class MechJson implements Serializable {
      public String name;
      public int texture_color;
      public List<PomkotsDataPack.PartJson> parts;
   }

   public class PartJson implements Serializable {
      public int slot;
      public String item;
      public int level;
   }

   public static class PartsData implements Serializable {
      public String id;
      public int weight;
      public String description;
      public List<PomkotsDataPack.LevelData> levels = new ArrayList<>();
      public List<List<PomkotsDataPack.SerializablePair<String, Integer>>> recipes = new ArrayList<>();

      @Override
      public String toString() {
         return "{" + this.id + "," + this.weight + "," + this.levels.toString() + "," + this.recipes.toString() + "}";
      }
   }

   public static class SerializablePair<F, S> implements Serializable {
      private final F first;
      private final S second;

      public SerializablePair(F first, S second) {
         this.first = first;
         this.second = second;
      }

      public F getFirst() {
         return this.first;
      }

      public S getSecond() {
         return this.second;
      }

      @Override
      public String toString() {
         return "[" + this.first + "," + this.second + "]";
      }
   }

   public static class TraderPoolItem implements Serializable {
      public String category;
      public String offer_item;
      public int offer_count;
      public String price_item;
      public int price_count;
      public int weight;
   }
}
