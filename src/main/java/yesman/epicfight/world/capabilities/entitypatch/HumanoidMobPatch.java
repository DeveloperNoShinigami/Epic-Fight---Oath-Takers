package yesman.epicfight.world.capabilities.entitypatch;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;
import yesman.epicfight.api.animation.AnimationProvider;
import yesman.epicfight.api.animation.Animator;
import yesman.epicfight.api.animation.LivingMotion;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.MobCombatBehaviors;
import yesman.epicfight.model.armature.HumanoidArmature;
import yesman.epicfight.network.EpicFightNetworkManager;
import yesman.epicfight.network.server.SPChangeLivingMotion;
import yesman.epicfight.world.capabilities.item.CapabilityItem;
import yesman.epicfight.world.capabilities.item.CapabilityItem.WeaponCategories;
import yesman.epicfight.world.capabilities.item.Style;
import yesman.epicfight.world.capabilities.item.WeaponCategory;
import yesman.epicfight.world.damagesource.StunType;
import yesman.epicfight.world.entity.ai.attribute.EpicFightAttributes;
import yesman.epicfight.world.entity.ai.goal.AnimatedAttackGoal;
import yesman.epicfight.world.entity.ai.goal.CombatBehaviors;
import yesman.epicfight.world.entity.ai.goal.TargetChasingGoal;

public abstract class HumanoidMobPatch<T extends PathfinderMob> extends MobPatch<T> {
	protected Map<WeaponCategory, Map<Style, Set<Pair<LivingMotion, StaticAnimation>>>> weaponLivingMotions;
	protected Map<WeaponCategory, Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>>> weaponAttackMotions;
	
	public HumanoidMobPatch(Faction faction) {
		super(faction);
		this.setWeaponMotions();
	}
	
	@Override
	public HumanoidArmature getArmature() {
		return (HumanoidArmature)this.armature;
	}
	
	@Override
	protected void initAI() {
		super.initAI();
		
		if (this.original.getVehicle() != null && this.original.getVehicle() instanceof Mob) {
			this.setAIAsMounted(this.original.getVehicle());
		} else {
			this.setAIAsInfantry(this.original.getMainHandItem().getItem() instanceof ProjectileWeaponItem);
		}
	}
	
	@Override
	public void onStartTracking(ServerPlayer trackingPlayer) {
		this.modifyLivingMotionByCurrentItem();
	}
	
	protected void setWeaponMotions() {
		this.weaponLivingMotions = Maps.newHashMap();
		this.weaponLivingMotions.put(WeaponCategories.GREATSWORD, ImmutableMap.of(
			CapabilityItem.Styles.TWO_HAND, Set.of(
				Pair.of(LivingMotions.WALK, Animations.BIPED_WALK_TWOHAND),
				Pair.of(LivingMotions.CHASE, Animations.BIPED_WALK_TWOHAND)
			)
		));
		
		this.weaponAttackMotions = Maps.newHashMap();
		this.weaponAttackMotions.put(WeaponCategories.AXE, ImmutableMap.of(CapabilityItem.Styles.COMMON, MobCombatBehaviors.HUMANOID_ONEHAND_TOOLS));
		this.weaponAttackMotions.put(WeaponCategories.HOE, ImmutableMap.of(CapabilityItem.Styles.COMMON, MobCombatBehaviors.HUMANOID_ONEHAND_TOOLS));
		this.weaponAttackMotions.put(WeaponCategories.PICKAXE, ImmutableMap.of(CapabilityItem.Styles.COMMON, MobCombatBehaviors.HUMANOID_ONEHAND_TOOLS));
		this.weaponAttackMotions.put(WeaponCategories.SHOVEL, ImmutableMap.of(CapabilityItem.Styles.COMMON, MobCombatBehaviors.HUMANOID_ONEHAND_TOOLS));
		this.weaponAttackMotions.put(WeaponCategories.SWORD, ImmutableMap.of(CapabilityItem.Styles.ONE_HAND, MobCombatBehaviors.HUMANOID_ONEHAND_TOOLS, CapabilityItem.Styles.TWO_HAND, MobCombatBehaviors.HUMANOID_DUAL_SWORD));
		this.weaponAttackMotions.put(WeaponCategories.GREATSWORD, ImmutableMap.of(CapabilityItem.Styles.TWO_HAND, MobCombatBehaviors.HUMANOID_GREATSWORD));
		this.weaponAttackMotions.put(WeaponCategories.UCHIGATANA, ImmutableMap.of(CapabilityItem.Styles.TWO_HAND, MobCombatBehaviors.HUMANOID_KATANA));
		this.weaponAttackMotions.put(WeaponCategories.LONGSWORD, ImmutableMap.of(CapabilityItem.Styles.TWO_HAND, MobCombatBehaviors.HUMANOID_LONGSWORD));
		this.weaponAttackMotions.put(WeaponCategories.TACHI, ImmutableMap.of(CapabilityItem.Styles.TWO_HAND, MobCombatBehaviors.HUMANOID_TACHI));
		this.weaponAttackMotions.put(WeaponCategories.SPEAR, ImmutableMap.of(CapabilityItem.Styles.ONE_HAND, MobCombatBehaviors.HUMANOID_SPEAR_ONEHAND, CapabilityItem.Styles.TWO_HAND, MobCombatBehaviors.HUMANOID_SPEAR_TWOHAND));
		this.weaponAttackMotions.put(WeaponCategories.FIST, ImmutableMap.of(CapabilityItem.Styles.COMMON, MobCombatBehaviors.HUMANOID_FIST));
		this.weaponAttackMotions.put(WeaponCategories.DAGGER, ImmutableMap.of(CapabilityItem.Styles.ONE_HAND, MobCombatBehaviors.HUMANOID_ONEHAND_DAGGER, CapabilityItem.Styles.TWO_HAND, MobCombatBehaviors.HUMANOID_TWOHAND_DAGGER));
		this.weaponAttackMotions.put(WeaponCategories.RANGED, ImmutableMap.of(CapabilityItem.Styles.COMMON, MobCombatBehaviors.HUMANOID_FIST));
		this.weaponAttackMotions.put(WeaponCategories.TRIDENT, ImmutableMap.of(CapabilityItem.Styles.COMMON, MobCombatBehaviors.HUMANOID_SPEAR_ONEHAND));
	}
	
	protected CombatBehaviors.Builder<HumanoidMobPatch<?>> getHoldingItemWeaponMotionBuilder() {
		CapabilityItem itemCap = this.getHoldingItemCapability(InteractionHand.MAIN_HAND);
		
		if (this.weaponAttackMotions.containsKey(itemCap.getWeaponCategory())) {
			Map<Style, CombatBehaviors.Builder<HumanoidMobPatch<?>>> motionByStyle = this.weaponAttackMotions.get(itemCap.getWeaponCategory());
			Style style = itemCap.getStyle(this);
			
			if (motionByStyle.containsKey(style) || motionByStyle.containsKey(CapabilityItem.Styles.COMMON)) {
				return motionByStyle.getOrDefault(style, motionByStyle.get(CapabilityItem.Styles.COMMON));
			}
		}
		
		return this.original.getMainHandItem().isEmpty() ? MobCombatBehaviors.HUMANOID_FIST : MobCombatBehaviors.HUMANOID_ONEHAND_TOOLS;
	}
	
	public void setAIAsInfantry(boolean holdingRanedWeapon) {
		CombatBehaviors.Builder<HumanoidMobPatch<?>> builder = this.getHoldingItemWeaponMotionBuilder();
		
		if (builder != null) {
			this.original.goalSelector.addGoal(0, new AnimatedAttackGoal<>(this, builder.build(this)));
			this.original.goalSelector.addGoal(1, new TargetChasingGoal(this, this.getOriginal(), 1.0D, true));
		}
	}
	
	public void setAIAsMounted(Entity ridingEntity) {
		if (this.isArmed()) {
			if (ridingEntity instanceof AbstractHorse) {
				this.original.goalSelector.addGoal(0, new AnimatedAttackGoal<>(this, MobCombatBehaviors.MOUNT_HUMANOID_BEHAVIORS.build(this)));
				this.original.goalSelector.addGoal(1, new TargetChasingGoal(this, this.getOriginal(), 1.0D, true));
			}
		}
	}
	
	protected final void commonMobAnimatorInit(Animator animator) {
		animator.addLivingAnimation(LivingMotions.IDLE, Animations.BIPED_IDLE);
		animator.addLivingAnimation(LivingMotions.WALK, Animations.BIPED_WALK);
		animator.addLivingAnimation(LivingMotions.FALL, Animations.BIPED_FALL);
		animator.addLivingAnimation(LivingMotions.MOUNT, Animations.BIPED_MOUNT);
		animator.addLivingAnimation(LivingMotions.DEATH, Animations.BIPED_DEATH);
	}
	
	protected final void commonAggresiveMobAnimatorInit(Animator animator) {
		animator.addLivingAnimation(LivingMotions.IDLE, Animations.BIPED_IDLE);
		animator.addLivingAnimation(LivingMotions.WALK, Animations.BIPED_WALK);
		animator.addLivingAnimation(LivingMotions.CHASE, Animations.BIPED_WALK);
		animator.addLivingAnimation(LivingMotions.FALL, Animations.BIPED_FALL);
		animator.addLivingAnimation(LivingMotions.MOUNT, Animations.BIPED_MOUNT);
		animator.addLivingAnimation(LivingMotions.DEATH, Animations.BIPED_DEATH);
	}
	
	@Override
	public void updateHeldItem(CapabilityItem fromCap, CapabilityItem toCap, ItemStack from, ItemStack to, InteractionHand hand) {
		this.initAI();
		
		if (hand == InteractionHand.OFF_HAND) {
			if (!from.isEmpty()) {
				from.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_SPEED).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_ATTACK_SPEED.get())::removeModifier);
			}
			if (!fromCap.isEmpty()) {
				fromCap.getAttributeModifiers(EquipmentSlot.MAINHAND, this).get(Attributes.ATTACK_SPEED).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_ATTACK_SPEED.get())::removeModifier);
				fromCap.getAttributeModifiers(EquipmentSlot.MAINHAND, this).get(EpicFightAttributes.ARMOR_NEGATION.get()).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_ARMOR_NEGATION.get())::removeModifier);
				fromCap.getAttributeModifiers(EquipmentSlot.MAINHAND, this).get(EpicFightAttributes.IMPACT.get()).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_IMPACT.get())::removeModifier);
				fromCap.getAttributeModifiers(EquipmentSlot.MAINHAND, this).get(EpicFightAttributes.MAX_STRIKES.get()).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_MAX_STRIKES.get())::removeModifier);
			}
			
			if (!to.isEmpty()) {
				to.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_SPEED).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_ATTACK_SPEED.get())::addTransientModifier);
			}
			if (!toCap.isEmpty()) {
				toCap.getAttributeModifiers(EquipmentSlot.MAINHAND, this).get(Attributes.ATTACK_SPEED).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_ARMOR_NEGATION.get())::addTransientModifier);
				toCap.getAttributeModifiers(EquipmentSlot.MAINHAND, this).get(EpicFightAttributes.ARMOR_NEGATION.get()).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_ARMOR_NEGATION.get())::addTransientModifier);
				toCap.getAttributeModifiers(EquipmentSlot.MAINHAND, this).get(EpicFightAttributes.IMPACT.get()).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_IMPACT.get())::addTransientModifier);
				toCap.getAttributeModifiers(EquipmentSlot.MAINHAND, this).get(EpicFightAttributes.MAX_STRIKES.get()).forEach(this.original.getAttribute(EpicFightAttributes.OFFHAND_MAX_STRIKES.get())::addTransientModifier);
			}
		}
		
		this.modifyLivingMotionByCurrentItem();
		
		super.updateHeldItem(fromCap, toCap, from, to, hand);
	}
	
	public void modifyLivingMotionByCurrentItem() {
		this.getAnimator().resetLivingAnimations();
		
		CapabilityItem mainhandCap = this.getHoldingItemCapability(InteractionHand.MAIN_HAND);
		CapabilityItem offhandCap = this.getAdvancedHoldingItemCapability(InteractionHand.OFF_HAND);
		
		Map<LivingMotion, AnimationProvider<?>> motionModifier = new HashMap<>(mainhandCap.getLivingMotionModifier(this, InteractionHand.MAIN_HAND));
		motionModifier.putAll(offhandCap.getLivingMotionModifier(this, InteractionHand.OFF_HAND));
		
		for (Map.Entry<LivingMotion, AnimationProvider<?>> entry : motionModifier.entrySet()) {
			this.getAnimator().addLivingAnimation(entry.getKey(), entry.getValue().get());
		}
		
		if (this.weaponLivingMotions != null && this.weaponLivingMotions.containsKey(mainhandCap.getWeaponCategory())) {
			Map<Style, Set<Pair<LivingMotion, StaticAnimation>>> mapByStyle = this.weaponLivingMotions.get(mainhandCap.getWeaponCategory());
			Style style = mainhandCap.getStyle(this);
			
			if (mapByStyle.containsKey(style) || mapByStyle.containsKey(CapabilityItem.Styles.COMMON)) {
				Set<Pair<LivingMotion, StaticAnimation>> animModifierSet = mapByStyle.getOrDefault(style, mapByStyle.get(CapabilityItem.Styles.COMMON));
				
				for (Pair<LivingMotion, StaticAnimation> pair : animModifierSet) {
					this.animator.addLivingAnimation(pair.getFirst(), pair.getSecond());
				}
			}
		}
		
		SPChangeLivingMotion msg = new SPChangeLivingMotion(this.original.getId());
		msg.putEntries(this.getAnimator().getLivingAnimations().entrySet());
		EpicFightNetworkManager.sendToAllPlayerTrackingThisEntity(msg, this.original);
	}
	
	public boolean isArmed() {
		Item heldItem = this.original.getMainHandItem().getItem();
		return heldItem instanceof SwordItem || heldItem instanceof DiggerItem || heldItem instanceof TridentItem;
	}
	
	@Override
	public void onMount(boolean isMountOrDismount, Entity ridingEntity) {
		if (this.original == null) {
			return;
		}
		
		if (!this.original.level().isClientSide() && !this.original.isNoAi()) {
			Set<Goal> toRemove = Sets.newHashSet();
			this.selectGoalToRemove(toRemove);
			toRemove.forEach(this.original.goalSelector::removeGoal);
			
			if (isMountOrDismount) {
				this.setAIAsMounted(ridingEntity);
			} else {
				this.setAIAsInfantry(this.original.getMainHandItem().getItem() instanceof ProjectileWeaponItem);
			}
		}
	}
	
	@Override
	public StaticAnimation getHitAnimation(StunType stunType) {
		if (this.original.getVehicle() != null) {
			return Animations.BIPED_HIT_ON_MOUNT;
		} else {
			switch (stunType) {
			case LONG:
				return Animations.BIPED_HIT_LONG;
			case SHORT:
				return Animations.BIPED_HIT_SHORT;
			case HOLD:
				return Animations.BIPED_HIT_SHORT;
			case KNOCKDOWN:
				return Animations.BIPED_KNOCKDOWN;
			case NEUTRALIZE:
				return Animations.BIPED_COMMON_NEUTRALIZED;
			case FALL:
				return Animations.BIPED_LANDING;
			case NONE:
				return null;
			}
		}
		
		return null;
	}
}