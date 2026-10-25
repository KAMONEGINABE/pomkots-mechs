package grcmcs.minecraft.mods.pomkotsmechs.misc.arena.dto;

import grcmcs.minecraft.mods.pomkotsmechs.PomkotsMechs;
import grcmcs.minecraft.mods.pomkotsmechs.entity.vehicle.custom.Pmvc01Entity;
import grcmcs.minecraft.mods.pomkotsmechs.items.parts.BasePartsItem;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public class ArenaMechData {
   private String mechName;
   private int textureColor;
   private NonNullList<ItemStack> parts = NonNullList.m_122780_(18, ItemStack.f_41583_);

   public String getMechName() {
      return this.mechName;
   }

   public void setMechName(String mechName) {
      this.mechName = mechName;
   }

   public NonNullList<ItemStack> getParts() {
      return this.parts;
   }

   public void setParts(NonNullList<ItemStack> parts) {
      this.parts = parts;
   }

   public int getTextureColor() {
      return this.textureColor;
   }

   public void setTextureColor(int textureColor) {
      this.textureColor = textureColor;
   }

   public CompoundTag save() {
      CompoundTag tag = new CompoundTag();
      tag.m_128359_("MechName", this.mechName);
      tag.m_128405_("TextureColor", this.textureColor);
      ListTag partsList = new ListTag();

      for (int i = 0; i < this.parts.size(); i++) {
         ItemStack stack = (ItemStack)this.parts.get(i);
         if (!stack.m_41619_()) {
            CompoundTag stackTag = new CompoundTag();
            stackTag.m_128405_("Slot", i);
            if (stack.m_41720_() instanceof BasePartsItem basePartsItem) {
               stackTag.m_128405_(PomkotsMechs.nbtName("Level"), basePartsItem.getLevel(stack));
            }

            stack.m_41739_(stackTag);
            partsList.add(stackTag);
         }
      }

      tag.m_128365_("Parts", partsList);
      return tag;
   }

   public static ArenaMechData load(CompoundTag tag) {
      ArenaMechData data = new ArenaMechData();
      data.mechName = tag.m_128461_("MechName");
      data.textureColor = tag.m_128451_("TextureColor");
      ListTag partsList = tag.m_128437_("Parts", 10);
      NonNullList<ItemStack> parts = NonNullList.m_122780_(18, ItemStack.f_41583_);

      for (Tag element : partsList) {
         CompoundTag stackTag = (CompoundTag)element;
         int slot = stackTag.m_128451_("Slot");
         parts.set(slot, ItemStack.m_41712_(stackTag));
      }

      data.parts = parts;
      return data;
   }

   public static ArenaMechData fromMechInstance(Pmvc01Entity mech) {
      ArenaMechData data = new ArenaMechData();
      Component cName = mech.m_7770_();
      if (cName == null) {
         data.setMechName("No Name");
      } else {
         data.setMechName(cName.getString());
      }

      data.setTextureColor(mech.getTextureColor());
      NonNullList<ItemStack> parts = data.parts;
      parts.set(0, mech.getHeadParts());
      parts.set(1, mech.getBodyParts());
      parts.set(2, mech.getArmParts());
      parts.set(3, mech.getLegsParts());
      parts.set(4, mech.getGenerator());
      parts.set(5, mech.getBooster());
      parts.set(6, mech.getRightArmWeapon());
      parts.set(7, mech.getLeftArmWeapon());
      parts.set(8, mech.getRightShoulderWeapon());
      parts.set(9, mech.getLeftShoulderWeapon());
      parts.set(10, mech.getExtension1Weapon());
      parts.set(11, mech.getExtension1Weapon());
      return data;
   }
}
