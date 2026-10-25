package grcmcs.minecraft.mods.pomkotsmechs.items.circuits.core.mechstats;

public class MechStats {
   private double health;
   private double knockBackResistance;
   private double energyCapacity;
   private double energyRecovery;
   private double energyCost;
   private double moveSpeed;
   private double verticalSpeed;
   private double dashSpeed;
   private double jumpPower;
   private double damageAll;
   private double rifleDamage;
   private double machineGunDamage;
   private double shotgunDamage;
   private double missileDamage;
   private double grenadeDamage;
   private double meleeDamage;

   public static MechStats defaultMech() {
      return new MechStats()
         .health(1.0)
         .knockBackResistance(1.0)
         .energyCapacity(1.0)
         .energyRecovery(1.0)
         .energyCost(1.0)
         .moveSpeed(1.0)
         .verticalSpeed(1.0)
         .dashSpeed(1.0)
         .jumpPower(1.0)
         .damageAll(1.0)
         .rifleDamage(1.0)
         .machineGunDamage(1.0)
         .shotgunDamage(1.0)
         .missileDamage(1.0)
         .grenadeDamage(1.0)
         .meleeDamage(1.0);
   }

   public MechStats() {
   }

   public MechStats(MechStats other) {
      this.health = other.health;
      this.knockBackResistance = other.knockBackResistance;
      this.energyCapacity = other.energyCapacity;
      this.energyRecovery = other.energyRecovery;
      this.energyCost = other.energyCost;
      this.moveSpeed = other.moveSpeed;
      this.verticalSpeed = other.verticalSpeed;
      this.dashSpeed = other.dashSpeed;
      this.jumpPower = other.jumpPower;
      this.damageAll = other.damageAll;
      this.rifleDamage = other.rifleDamage;
      this.machineGunDamage = other.machineGunDamage;
      this.shotgunDamage = other.shotgunDamage;
      this.missileDamage = other.missileDamage;
      this.grenadeDamage = other.grenadeDamage;
      this.meleeDamage = other.meleeDamage;
   }

   public static MechStats copyOf(MechStats other) {
      return new MechStats(other);
   }

   public double get(MechStatType statType) {
      return switch (statType) {
         case HEALTH -> this.health;
         case KNOCKBACK_RESISTANCE -> this.knockBackResistance;
         case ENERGY_CAPACITY -> this.energyCapacity;
         case ENERGY_RECOVERY -> this.energyRecovery;
         case ENERGY_COST -> this.energyCost;
         case MOVE_SPEED -> this.moveSpeed;
         case VERTICAL_SPEED -> this.verticalSpeed;
         case DASH_SPEED -> this.dashSpeed;
         case JUMP_POWER -> this.jumpPower;
         case DAMAGE_ALL -> this.damageAll;
         case DAMAGE_RIFLE -> this.rifleDamage;
         case DAMAGE_MACHINE_GUN -> this.machineGunDamage;
         case DAMAGE_SHOTGUN -> this.shotgunDamage;
         case DAMAGE_MISSILE -> this.missileDamage;
         case DAMAGE_GRENADE -> this.grenadeDamage;
         case DAMAGE_MELEE -> this.meleeDamage;
      };
   }

   public void set(MechStatType statType, double value) {
      switch (statType) {
         case HEALTH:
            this.health = value;
            break;
         case KNOCKBACK_RESISTANCE:
            this.knockBackResistance = value;
            break;
         case ENERGY_CAPACITY:
            this.energyCapacity = value;
            break;
         case ENERGY_RECOVERY:
            this.energyRecovery = value;
            break;
         case ENERGY_COST:
            this.energyCost = value;
            break;
         case MOVE_SPEED:
            this.moveSpeed = value;
            break;
         case VERTICAL_SPEED:
            this.verticalSpeed = value;
            break;
         case DASH_SPEED:
            this.dashSpeed = value;
            break;
         case JUMP_POWER:
            this.jumpPower = value;
            break;
         case DAMAGE_ALL:
            this.damageAll = value;
            break;
         case DAMAGE_RIFLE:
            this.rifleDamage = value;
            break;
         case DAMAGE_MACHINE_GUN:
            this.machineGunDamage = value;
            break;
         case DAMAGE_SHOTGUN:
            this.shotgunDamage = value;
            break;
         case DAMAGE_MISSILE:
            this.missileDamage = value;
            break;
         case DAMAGE_GRENADE:
            this.grenadeDamage = value;
            break;
         case DAMAGE_MELEE:
            this.meleeDamage = value;
      }
   }

   public void add(MechStatType statType, double value) {
      this.set(statType, this.get(statType) + value);
   }

   public void multiply(MechStatType statType, double multiplier) {
      this.set(statType, this.get(statType) * multiplier);
   }

   public MechStats copy() {
      return new MechStats(this);
   }

   public double health() {
      return this.health;
   }

   public MechStats health(double health) {
      this.health = health;
      return this;
   }

   public double energyCapacity() {
      return this.energyCapacity;
   }

   public MechStats energyCapacity(double energyCapacity) {
      this.energyCapacity = energyCapacity;
      return this;
   }

   public double energyRecovery() {
      return this.energyRecovery;
   }

   public MechStats energyRecovery(double energyRecovery) {
      this.energyRecovery = energyRecovery;
      return this;
   }

   public double energyCost() {
      return this.energyCost;
   }

   public MechStats energyCost(double energyCost) {
      this.energyCost = energyCost;
      return this;
   }

   public double moveSpeed() {
      return this.moveSpeed;
   }

   public MechStats moveSpeed(double moveSpeed) {
      this.moveSpeed = moveSpeed;
      return this;
   }

   public double verticalSpeed() {
      return this.verticalSpeed;
   }

   public MechStats verticalSpeed(double verticalSpeed) {
      this.verticalSpeed = verticalSpeed;
      return this;
   }

   public double dashSpeed() {
      return this.dashSpeed;
   }

   public MechStats dashSpeed(double dashSpeed) {
      this.dashSpeed = dashSpeed;
      return this;
   }

   public double jumpPower() {
      return this.jumpPower;
   }

   public MechStats jumpPower(double jumpPower) {
      this.jumpPower = jumpPower;
      return this;
   }

   public double damageAll() {
      return this.damageAll;
   }

   public MechStats damageAll(double damageAll) {
      this.damageAll = damageAll;
      return this;
   }

   public double rifleDamage() {
      return this.rifleDamage;
   }

   public MechStats rifleDamage(double rifleDamage) {
      this.rifleDamage = rifleDamage;
      return this;
   }

   public double machineGunDamage() {
      return this.machineGunDamage;
   }

   public MechStats machineGunDamage(double machineGunDamage) {
      this.machineGunDamage = machineGunDamage;
      return this;
   }

   public double shotgunDamage() {
      return this.shotgunDamage;
   }

   public MechStats shotgunDamage(double shotgunDamage) {
      this.shotgunDamage = shotgunDamage;
      return this;
   }

   public double missileDamage() {
      return this.missileDamage;
   }

   public MechStats missileDamage(double missileDamage) {
      this.missileDamage = missileDamage;
      return this;
   }

   public double grenadeDamage() {
      return this.grenadeDamage;
   }

   public MechStats grenadeDamage(double grenadeDamage) {
      this.grenadeDamage = grenadeDamage;
      return this;
   }

   public double meleeDamage() {
      return this.meleeDamage;
   }

   public MechStats meleeDamage(double meleeDamage) {
      this.meleeDamage = meleeDamage;
      return this;
   }

   public double knockBackResistance() {
      return this.knockBackResistance;
   }

   public MechStats knockBackResistance(double knockBackResistance) {
      this.knockBackResistance = knockBackResistance;
      return this;
   }
}
