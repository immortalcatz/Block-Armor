package twopiradians.blockArmor.common.seteffect;

import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import twopiradians.blockArmor.common.item.ArmorSet;
import twopiradians.blockArmor.common.item.ItemBlockArmor;

public class SetEffectFiery extends SetEffect {

	protected SetEffectFiery() {
		this.color = TextFormatting.RED;
		this.description = "Ignites enemies after attacking or being attacked";
		MinecraftForge.EVENT_BUS.register(this);
	}

	/**Ignites attackers/attackees*/
	@SubscribeEvent
	public void onEvent(LivingAttackEvent event) {		
		if (event.getSource().getSourceOfDamage() instanceof EntityLivingBase 
				&& !event.getSource().getSourceOfDamage().worldObj.isRemote) {
			EntityLivingBase attacker = (EntityLivingBase) event.getSource().getSourceOfDamage();
			EntityLivingBase attacked = event.getEntityLiving();

			//Lights the entity that attacks the wearer of the armor
			ItemStack stack = ArmorSet.getFirstSetItem(attacked, this);
			ArmorSet set = stack == null ? null : ((ItemBlockArmor)stack.getItem()).set;
			if (ArmorSet.isSetEffectEnabled(set) && !attacker.isInWater())	{
				if (!attacker.isBurning())
					attacker.worldObj.playSound(null, attacker.posX, 
							attacker.posY, attacker.posZ, SoundEvents.ITEM_FIRECHARGE_USE, 
							SoundCategory.PLAYERS, 1.0f, attacker.worldObj.rand.nextFloat());
				attacker.setFire(5);
			}
			//Lights the target of the wearer when the wearer attacks
			stack = ArmorSet.getFirstSetItem(attacker, this);
			set = stack == null ? null : ((ItemBlockArmor)stack.getItem()).set;
			if (ArmorSet.isSetEffectEnabled(set) && !attacked.isInWater())	{
				if (!attacked.isBurning())
					attacker.worldObj.playSound(null, attacked.posX, attacked.posY, attacked.posZ, 
							SoundEvents.ITEM_FIRECHARGE_USE, SoundCategory.PLAYERS, 1.0f, attacker.worldObj.rand.nextFloat());
				attacked.setFire(5);
			}
		}
	}
	
	/**Should block be given this set effect*/
	@Override
	protected boolean isValid(Block block, int meta) {		
		if (SetEffect.registryNameContains(block, new String[] {"netherrack", "magma", "fire", "flame", "lava"}))
			return true;		
		return false;
	}
}