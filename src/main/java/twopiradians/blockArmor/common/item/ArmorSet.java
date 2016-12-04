package twopiradians.blockArmor.common.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;

import com.google.common.collect.Maps;

import gnu.trove.map.hash.TIntObjectHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockOre;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.ItemModelMesherForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import twopiradians.blockArmor.common.BlockArmor;
import twopiradians.blockArmor.common.config.Config;

public class ArmorSet {

	public static ArrayList<ArmorSet> allSets;
	/**Map of sets that have been auto generated and whether or not they are enabled in config*/
	public static HashMap<ArmorSet, Boolean> autoGeneratedSets = Maps.newHashMap();
	/**Map of sets that have effects and whether or not their effect is enabled*/
	public static HashMap<ArmorSet, Boolean> setsWithEffects = Maps.newHashMap();
	public static final ArrayList<ArmorSet> MANUALLY_ADDED_SETS;
	static {
		MANUALLY_ADDED_SETS = new ArrayList<ArmorSet>() {{
			add(new ArmorSet(new ItemStack(Blocks.NETHERRACK, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.OBSIDIAN, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.REDSTONE_BLOCK, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.SNOW, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.LAPIS_BLOCK, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.END_STONE, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.SLIME_BLOCK, 1, 0), true));
			add(new ArmorSet(new ItemStack(Items.REEDS, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.PRISMARINE, 1, 2), true));
			add(new ArmorSet(new ItemStack(Blocks.EMERALD_BLOCK, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.BRICK_BLOCK, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.BEDROCK, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.QUARTZ_BLOCK, 1, 0), true));
			add(new ArmorSet(new ItemStack(Blocks.BROWN_MUSHROOM_BLOCK, 1, 0), false));
			add(new ArmorSet(new ItemStack(Blocks.RED_MUSHROOM_BLOCK, 1, 0), false));
		}};
	}
	/**Reflected value used to find textures*/
	private static ItemModelMesherForge itemModelMesher;
	/**Reflected value used to iterate through all items*/
	private static IdentityHashMap<Item, TIntObjectHashMap<ModelResourceLocation>> locations;
	/**List of current display names of all registered items - to prevent redundant sets*/
	private static ArrayList<String> displayNames;

	public ItemStack stack;
	public Item item;
	public int meta;
	public Block block;
	public ArmorMaterial material;      
	public boolean hasSetEffect;
	public ItemModArmor helmet;
	public ItemModArmor chestplate;
	public ItemModArmor leggings;
	public ItemModArmor boots;

	private ResourceLocation helmetTexture;
	private ResourceLocation chestplateTexture;
	private ResourceLocation leggingsTexture;
	private ResourceLocation bootsTexture;


	@SuppressWarnings("deprecation")
	public ArmorSet(ItemStack stack, boolean hasSetEffect) {
		this.stack = stack;
		this.item = stack.getItem();
		this.meta = stack.getMetadata();
		if (item == Items.REEDS)
			this.block = Blocks.REEDS;
		else
			this.block = ((ItemBlock) item).getBlock();
		this.hasSetEffect = hasSetEffect;
		if (hasSetEffect)
			setsWithEffects.put(this, true);
		//calculate values for and set material
		float blockHardness = 0; 
		double durability = 5;
		float toughness = 0;
		int enchantability = 12;

		try {
			blockHardness = this.block.getBlockHardness(this.block.getDefaultState(), null, new BlockPos(0,0,0));
		} catch(Exception e) {
			blockHardness = ReflectionHelper.getPrivateValue(Block.class, this.block, 11); //blockHardness
		}
		if(blockHardness == -1) {
			durability = 0;
			blockHardness = 1000;
		}
		else
			durability = 2 + 8* Math.log(blockHardness + 1);
		if (blockHardness > 10)
			toughness = Math.min(blockHardness / 10F, 10);
		durability = Math.min(30, durability);
		blockHardness = (float) Math.log(blockHardness+1.5D)+1;
		int reductionAmount1 = (int) Math.min(1 + blockHardness, 3);
		int reductionAmount2 = (int) Math.min(1 + 2*blockHardness, 5);
		int reductionAmount3 = (int) Math.min(1 + 2.2D*blockHardness, 6);
		int reductionAmount4 = (int) Math.min(1 + blockHardness, 3);
		int[] reductionAmounts = new int[] {reductionAmount1, reductionAmount2, reductionAmount3, reductionAmount4};
		this.material = EnumHelper.addArmorMaterial(getItemStackDisplayName(stack, null)+" Material", "", 
				(int) durability, reductionAmounts, enchantability, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, toughness);
		//System.out.println(getItemStackDisplayName(stack, null)+": blockHardness = "+blockHardness+", toughness = "+toughness+", durability = "+durability);
	}

	/**Creates ArmorSets for each valid registered item and puts them in allSets*/
	public static void postInit() {
		//initialize reflected fields TODO fix to not use Minecraft.getMinecraft()
		itemModelMesher = ReflectionHelper.getPrivateValue(RenderItem.class, Minecraft.getMinecraft().getRenderItem(), 3);
		locations = ReflectionHelper.getPrivateValue(ItemModelMesherForge.class, itemModelMesher, 0);

		displayNames = new ArrayList<String>();
		for (Item item : locations.keySet())
			for (int meta : locations.get(item).keys())
				if (item != null)
					displayNames.add(new ItemStack(item, 1, meta).getDisplayName());

		allSets = new ArrayList<ArmorSet>();
		allSets.addAll(MANUALLY_ADDED_SETS);
		for (Item item : locations.keySet()) //iterate through all items and meta and create sets for valid ones
			for (int meta : locations.get(item).keys()) {
				ItemStack stack = new ItemStack(item, 1, meta);
				if (isValid(stack) && ArmorSet.getSet(item, meta) == null) 
					if (!displayNames.contains(getItemStackDisplayName(stack, EntityEquipmentSlot.HEAD)) &&
							!displayNames.contains(getItemStackDisplayName(stack, EntityEquipmentSlot.CHEST)) &&
							!displayNames.contains(getItemStackDisplayName(stack, EntityEquipmentSlot.LEGS)) &&
							!displayNames.contains(getItemStackDisplayName(stack, EntityEquipmentSlot.FEET))) {
						allSets.add(new ArmorSet(stack, false));
						displayNames.add(getItemStackDisplayName(stack, EntityEquipmentSlot.HEAD));
						displayNames.add(getItemStackDisplayName(stack, EntityEquipmentSlot.CHEST));
						displayNames.add(getItemStackDisplayName(stack, EntityEquipmentSlot.LEGS));
						displayNames.add(getItemStackDisplayName(stack, EntityEquipmentSlot.FEET));
					}
			}

		//populate autoGeneratedSets
		for (ArmorSet set : allSets)
			if (!MANUALLY_ADDED_SETS.contains(set))
				autoGeneratedSets.put(set, true);
	}

	/**Returns ResourceLocation to texture corresponding to given ItemModArmor*/
	public static ResourceLocation getTextureLocation(ItemModArmor item) {
		ArmorSet set = ArmorSet.getSet(item);
		if (set != null) {
			switch (item.getEquipmentSlot()) {
			case HEAD:
				return set.helmetTexture;
			case CHEST:
				return set.chestplateTexture;
			case LEGS:
				return set.leggingsTexture;
			case FEET:
				return set.bootsTexture;
			default:
				break;
			}
		}
		return null;
	}

	/**Change display name based on the block*/
	public static String getItemStackDisplayName(ItemStack stack, EntityEquipmentSlot slot)
	{
		String name;
		if (stack.getItem() instanceof ItemModArmor) {
			ArmorSet set = ArmorSet.getSet((ItemModArmor) stack.getItem());
			name = set.stack.getDisplayName();
		}
		else
			name = stack.getDisplayName();

		if (slot != null)
			switch (slot) {
			case HEAD:
				name += " Helmet";
				break;
			case CHEST:
				name += " Chestplate";
				break;
			case LEGS:
				name += " Leggings";
				break;
			case FEET:
				name += " Boots";
				break;
			default:
				break;
			}

		return name.replace("Block of ", "").replace("Block ", "").replace("Gold", "Golden");
	}

	/**Determines if entity is wearing a full set of armor of same material*/
	public static boolean isWearingFullSet(EntityLivingBase entity, ArmorSet set)
	{
		if (entity != null && set != null
				&& entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD) != null
				&& entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST) != null
				&& entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS) != null
				&& entity.getItemStackFromSlot(EntityEquipmentSlot.FEET) != null
				&& entity.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() == set.boots
				&& entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() == set.leggings
				&& entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == set.chestplate
				&& entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == set.helmet)
			return true;
		else
			return false;
	}

	/**Returns true if the set has a set effect and is enabled in Config*/
	public static boolean isSetEffectEnabled(ArmorSet set) {
		if (set == null || !set.hasSetEffect || Config.setEffects == 1)
			return false;
		if (setsWithEffects.get(set) || Config.setEffects == 0)
			return true;
		return false;
	}

	/**Returns armor set corresponding to given block and meta, or null if none exists*/
	public static ArmorSet getSet(Block block, int meta) {
		return getSet(Item.getItemFromBlock(block), meta);
	}

	/**Returns armor set corresponding to given item and meta, or null if none exists*/
	public static ArmorSet getSet(Item item, int meta) {
		for (ArmorSet set : allSets)
			if (set.item == item && set.meta == meta)
				return set;
		return null;
	}

	/**Returns armor set containing given ItemModArmor, or null if none exists*/
	public static ArmorSet getSet(ItemModArmor item) {
		for (ArmorSet set : allSets)
			if (set.helmet == item || set.chestplate == item || set.leggings == item || set.boots == item)
				return set;
		return null;
	}

	/**Should an armor set be made from this item*/
	@SuppressWarnings("deprecation")
	private static boolean isValid(ItemStack stack) {
		if (stack == null || !(stack.getItem() instanceof ItemBlock) || stack.getDisplayName().contains("Ore"))
			return false;

		Block block = ((ItemBlock)stack.getItem()).getBlock();
		if (block instanceof BlockLiquid || block instanceof BlockContainer || block.hasTileEntity() || 
				block instanceof BlockOre || block instanceof BlockCrops || block instanceof BlockBush ||
				block == Blocks.BARRIER || block instanceof BlockLeaves || block == Blocks.MONSTER_EGG)
			return false;

		//Check if full block (requires player) (possibly check if block's model is for a full block? - accept only parent: block/cube*)
		ArrayList<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
		try {
			block.addCollisionBoxToList(block.getDefaultState(), null, new BlockPos(0,0,0), Block.FULL_BLOCK_AABB, list, null);
		} catch (Exception e) {
			return false;
		}
		if (list.size() != 1 || !list.get(0).equals(Block.FULL_BLOCK_AABB)) 
			return false;

		return true;
	}

	/**Gets item's textures if valid, otherwise returns null
	 * 
	 * @return ArrayList of ResourceLocations for helmet, chestplate, leggings, and boots*/
	@SideOnly(Side.CLIENT)
	public void initTextures() {
		ResourceLocation helmetTexture = null;
		ResourceLocation chestTexture = null;
		ResourceLocation leggingsTexture = null;
		ResourceLocation bootsTexture = null;

		//Gets textures from item model's BakedQuads (textures for each side)
		IBlockState state = this.block.getDefaultState();
		List<BakedQuad> list = new ArrayList<BakedQuad>();
		list.addAll(itemModelMesher.getItemModel(this.stack).getQuads(state, null, 0));
		for (EnumFacing facing : EnumFacing.VALUES)
			list.addAll(itemModelMesher.getItemModel(this.stack).getQuads(state, facing, 0));
		if (list.size() < 6)
			System.out.println("less than 6 textures! - I did not expect this!"); //TODO remove after testing
		for (BakedQuad quad : list) { //there's at least one texture per face
			switch (quad.getFace()) {
			case DOWN:
				if (bootsTexture != null)
					break;
				bootsTexture = new ResourceLocation(quad.getSprite().getIconName());
				bootsTexture = new ResourceLocation(bootsTexture.getResourceDomain(), "textures/"+bootsTexture.getResourcePath()+".png");
				break;
			case EAST:
				break;
			case NORTH:
				if (chestTexture != null)
					break;
				chestTexture = new ResourceLocation(quad.getSprite().getIconName());
				chestTexture = new ResourceLocation(chestTexture.getResourceDomain(), "textures/"+chestTexture.getResourcePath()+".png");
				break;
			case SOUTH:
				if (leggingsTexture != null)
					break;
				leggingsTexture = new ResourceLocation(quad.getSprite().getIconName());
				leggingsTexture = new ResourceLocation(leggingsTexture.getResourceDomain(), "textures/"+leggingsTexture.getResourcePath()+".png");
				break;
			case UP:
				if (helmetTexture != null)
					break;
				helmetTexture = new ResourceLocation(quad.getSprite().getIconName());
				helmetTexture = new ResourceLocation(helmetTexture.getResourceDomain(), "textures/"+helmetTexture.getResourcePath()+".png");
				break;
			case WEST:
				break;
			}
		}

		if (helmetTexture == null || chestTexture == null || leggingsTexture == null || bootsTexture == null) 
			System.out.println("null texture - this shouldn't happen!"); //TODO remove after testing

		ResourceLocation texture1 = new ResourceLocation(BlockArmor.MODID+":textures/models/armor/"+stack.getDisplayName().toLowerCase().replace(" ", "_")+"_layer_1.png");
		ResourceLocation texture2 = new ResourceLocation(BlockArmor.MODID+":textures/models/armor/"+stack.getDisplayName().toLowerCase().replace(" ", "_")+"_layer_2.png");
		try {
			Minecraft.getMinecraft().getResourceManager().getResource(texture1);
			this.helmetTexture = texture1;
			this.chestplateTexture = texture1;
			this.leggingsTexture = texture2;
			this.bootsTexture = texture1;
			System.out.println("Texture found at: "+texture1.toString());
		} catch (Exception e) {
			this.helmetTexture = helmetTexture;
			this.chestplateTexture = chestTexture;
			this.leggingsTexture = leggingsTexture;
			this.bootsTexture = bootsTexture;
			System.out.println("No texture found at: "+texture1.toString());
		}

		/*
		//set item inventory textures
		IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(new ItemStack(this.helmet));
		//System.out.println("");
		model.getParticleTexture();
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(new ItemStack(this.helmet)).getParticleTexture();
		//System.out.println("");

		 */
		//can change model, but not texture
		/*IItemPropertyGetter getter = new IItemPropertyGetter()
		{
			@SideOnly(Side.CLIENT)
			public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn)
			{
				return 1;
			}
		};

		ItemModArmor[] armors = new ItemModArmor[] {this.helmet, this.chestplate, this.leggings, this.boots};
		for (ItemModArmor item : armors) {
			item.addPropertyOverride(ArmorSet.getTextureLocation(item), getter); 

			IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(new ItemStack(item));
			//ResourceLocation loc = new ModelResourceLocation(BlockArmor.MODID+":" + "auto_generated_boots" , "inventory");
			List<ItemOverride> overrides = ReflectionHelper.getPrivateValue(ItemOverrideList.class, model.getOverrides(), 1); //overrides
			Map<ResourceLocation, Float> map = Maps.<ResourceLocation, Float>newLinkedHashMap();
			map.put(ArmorSet.getTextureLocation(item), 1F);
			overrides.add(new ItemOverride(ArmorSet.getTextureLocation(item), map));
			overrides = ReflectionHelper.getPrivateValue(ItemOverrideList.class, model.getOverrides(), 1); //overrides
			System.out.println(overrides.size());
		}*/
	}


}