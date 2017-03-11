package twopiradians.blockArmor.common.seteffect;

import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.common.item.ArmorSet;

public class SetEffectPusher extends SetEffect {

	protected SetEffectPusher() {
		this.color = TextFormatting.GRAY;
		this.description = "Pushes away nearby entities";
		this.usesButton = true;
	}

	/**Only called when player wearing full, enabled set*/
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
		super.onArmorTick(world, player, stack);

		if (ArmorSet.getFirstSetItem(player, this) == stack &&
				BlockArmor.key.isKeyDown(player) && !player.getCooldownTracker().hasCooldown(stack.getItem())) {
			AxisAlignedBB aabb = player.getEntityBoundingBox().expand(5, 5, 5);
			List<Entity> list = player.worldObj.getEntitiesWithinAABBExcludingEntity(player, aabb);

			if (!list.isEmpty()) {
				Iterator<Entity> iterator = list.iterator();            
				while (iterator.hasNext()) {
					Entity entityCollided = iterator.next();
					if (!entityCollided.isImmuneToExplosions()) {
						double xVel = entityCollided.posX - player.posX;
						double yVel = entityCollided.posY - player.posY;
						double zVel = entityCollided.posZ - player.posZ;
						double velScale = 5 / Math.sqrt(xVel * xVel + yVel * yVel + zVel * zVel);
						entityCollided.addVelocity(velScale*xVel, velScale*yVel, velScale*zVel); 
						entityCollided.velocityChanged = true;
					}
				}
				this.setCooldown(player, 40);
				world.playSound((EntityPlayer)null, player.getPosition(), SoundEvents.BLOCK_PISTON_EXTEND, 
						SoundCategory.PLAYERS, 1.0F, world.rand.nextFloat() + 0.5f);
			}
			else
				this.setCooldown(player, 5);
		}
	}

	/**Should block be given this set effect*/
	@Override
	protected boolean isValid(Block block, int meta) {		
		if (SetEffect.registryNameContains(block, new String[] {"piston", "push", "repel"}) &&
				!SetEffect.registryNameContains(block, new String[] {"sticky"}))
			return true;
		return false;
	}
}