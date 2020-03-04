/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.client;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.*;
import blusunrize.immersiveengineering.api.energy.ThermoelectricHandler;
import blusunrize.immersiveengineering.api.multiblocks.ManualElementMultiblock;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler;
import blusunrize.immersiveengineering.api.multiblocks.MultiblockHandler.IMultiblock;
import blusunrize.immersiveengineering.api.shader.ShaderCase;
import blusunrize.immersiveengineering.api.shader.ShaderCase.ShaderLayer;
import blusunrize.immersiveengineering.api.shader.ShaderRegistry;
import blusunrize.immersiveengineering.api.tool.BulletHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.ConveyorDirection;
import blusunrize.immersiveengineering.api.tool.ConveyorHandler.IConveyorBelt;
import blusunrize.immersiveengineering.api.tool.ExcavatorHandler;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.client.font.IEFontRender;
import blusunrize.immersiveengineering.client.font.NixieFontRender;
import blusunrize.immersiveengineering.client.fx.FluidSplashParticle.Data;
import blusunrize.immersiveengineering.client.fx.FractalParticle;
import blusunrize.immersiveengineering.client.fx.IEParticles;
import blusunrize.immersiveengineering.client.gui.*;
import blusunrize.immersiveengineering.client.manual.IEManualInstance;
import blusunrize.immersiveengineering.client.models.*;
import blusunrize.immersiveengineering.client.models.ModelConveyor.ConveyorLoader;
import blusunrize.immersiveengineering.client.models.ModelCoresample.CoresampleLoader;
import blusunrize.immersiveengineering.client.models.connection.*;
import blusunrize.immersiveengineering.client.models.multilayer.MultiLayerLoader;
import blusunrize.immersiveengineering.client.models.obj.IEOBJLoader;
import blusunrize.immersiveengineering.client.models.obj.IESmartObjModel;
import blusunrize.immersiveengineering.client.render.IEBipedLayerRenderer;
import blusunrize.immersiveengineering.client.render.IEOBJItemRenderer;
import blusunrize.immersiveengineering.client.render.entity.*;
import blusunrize.immersiveengineering.client.render.tile.*;
import blusunrize.immersiveengineering.common.CommonProxy;
import blusunrize.immersiveengineering.common.IEConfig;
import blusunrize.immersiveengineering.common.IEContent;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.IColouredBlock;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.MetalDevices;
import blusunrize.immersiveengineering.common.blocks.cloth.ShaderBannerTileEntity;
import blusunrize.immersiveengineering.common.blocks.metal.*;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.BasicConveyor;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.DropConveyor;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.SplitConveyor;
import blusunrize.immersiveengineering.common.blocks.metal.conveyors.VerticalConveyor;
import blusunrize.immersiveengineering.common.blocks.stone.CoresampleTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.ModWorkbenchTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.WatermillTileEntity;
import blusunrize.immersiveengineering.common.blocks.wooden.WindmillTileEntity;
import blusunrize.immersiveengineering.common.entities.*;
import blusunrize.immersiveengineering.common.gui.GuiHandler;
import blusunrize.immersiveengineering.common.items.DrillheadItem.DrillHeadPerm;
import blusunrize.immersiveengineering.common.items.IEItemInterfaces.IColouredItem;
import blusunrize.immersiveengineering.common.items.RevolverItem;
import blusunrize.immersiveengineering.common.util.IELogger;
import blusunrize.immersiveengineering.common.util.chickenbones.Matrix4;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import blusunrize.immersiveengineering.common.util.sound.IETileSound;
import blusunrize.immersiveengineering.common.util.sound.SkyhookSound;
import blusunrize.lib.manual.ManualElementTable;
import blusunrize.lib.manual.ManualEntry;
import blusunrize.lib.manual.ManualEntry.ManualEntryBuilder;
import blusunrize.lib.manual.ManualInstance;
import blusunrize.lib.manual.ManualUtils;
import blusunrize.lib.manual.Tree.InnerNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.ArrayListMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IHasContainer;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.gui.ScreenManager.IScreenFactory;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.particle.BreakingParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.DyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.particles.RedstoneParticleData;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Direction.AxisDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.ModelLoaderRegistry2;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.VersionChecker;
import net.minecraftforge.fml.VersionChecker.CheckResult;
import net.minecraftforge.fml.VersionChecker.Status;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.*;
import java.util.Map.Entry;

import static blusunrize.immersiveengineering.ImmersiveEngineering.MODID;
import static blusunrize.immersiveengineering.client.ClientUtils.mc;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Bus.MOD)
public class ClientProxy extends CommonProxy
{
	public static AtlasTexture revolverTextureMap;
	public static final ResourceLocation revolverTextureResource = new ResourceLocation("textures/atlas/immersiveengineering/revolvers.png");
	public static IEFontRender nixieFontOptional;
	public static IEFontRender nixieFont;
	public static IEFontRender itemFont;
	public static boolean stencilBufferEnabled = false;
	public static KeyBinding keybind_magnetEquip = new KeyBinding("key.immersiveengineering.magnetEquip", GLFW.GLFW_KEY_S, "key.categories.gameplay");
	public static KeyBinding keybind_chemthrowerSwitch = new KeyBinding("key.immersiveengineering.chemthrowerSwitch", 0, "key.categories.gameplay");

	@Override
	public void preInit()
	{
		Framebuffer fb = mc().getFramebuffer();
		/*TODO this probably needs to be readded to Forge
		if(GLX.isUsingFBOs()&&IEConfig.stencilBufferEnabled&&!fb.isStencilEnabled())
		{
			stencilBufferEnabled = fb.enableStencil();//Enabling FBO stencils
		}
		 */
		ModelLoaderRegistry2.registerLoader(new ResourceLocation(MODID, "ie_obj"), IEOBJLoader.instance);
		ModelLoaderRegistry2.registerLoader(ConnectionLoader.LOADER_NAME, new ConnectionLoader());
		OBJLoader.INSTANCE.addDomain("immersiveengineering");
		IEOBJLoader.instance.addDomain("immersiveengineering");

		RenderingRegistry.registerEntityRenderingHandler(RevolvershotEntity.class, RevolvershotRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(SkylineHookEntity.class, NoneRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(ChemthrowerShotEntity.class, ChemthrowerShotRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(RailgunShotEntity.class, RailgunShotRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(IEExplosiveEntity.class, IEExplosiveRenderer::new);
		RenderingRegistry.registerEntityRenderingHandler(FluorescentTubeEntity.class, FluorescentTubeRenderer::new);
		ModelLoaderRegistry.registerLoader(new FeedthroughLoader());
		ModelLoaderRegistry.registerLoader(new ModelConfigurableSides.Loader());
		ModelLoaderRegistry.registerLoader(new MultiLayerLoader());
		ModelLoaderRegistry.registerLoader(new CoresampleLoader());
		ModelLoaderRegistry.registerLoader(new ConveyorLoader());
	}

	@Override
	public void preInitEnd()
	{
		for(IECompatModule compat : IECompatModule.modules)
			try
			{
				compat.clientPreInit();
			} catch(Exception exception)
			{
				IELogger.error("Compat module for "+compat+" could not be client pre-initialized");
			}
	}

	@Override
	public void init()
	{
		super.init();
		ClientEventHandler handler = new ClientEventHandler();
		MinecraftForge.EVENT_BUS.register(handler);
		((IReloadableResourceManager)mc().getResourceManager()).addReloadListener(handler);

		keybind_magnetEquip.setKeyConflictContext(new IKeyConflictContext()
		{
			@Override
			public boolean isActive()
			{
				return mc().currentScreen==null;
			}

			@Override
			public boolean conflicts(IKeyConflictContext other)
			{
				return false;
			}
		});
		ClientRegistry.registerKeyBinding(keybind_magnetEquip);
		ShaderHelper.initShaders();

		keybind_chemthrowerSwitch.setKeyConflictContext(KeyConflictContext.IN_GAME);
		ClientRegistry.registerKeyBinding(keybind_chemthrowerSwitch);

		nixieFontOptional = new NixieFontRender(false);
		nixieFont = new NixieFontRender(false);
		itemFont = new IEFontRender(false);
		TeslaCoilTileEntity.effectMap = ArrayListMultimap.create();

		//TODO remove once the turntable is back, if it ever comes back
		DynamicModelLoader.requestTexture(new ResourceLocation(ImmersiveEngineering.MODID, "block/wooden_device/turntable_bottom"));

		ClientRegistry.bindTileEntitySpecialRenderer(ChargingStationTileEntity.class, new ChargingStationRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(SampleDrillTileEntity.class, new SampleDrillRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TeslaCoilTileEntity.class, new TeslaCoilRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TurretTileEntity.class, new TurretRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(BelljarTileEntity.class, new BelljarRenderer());
		// MULTIBLOCKS
		ClientRegistry.bindTileEntitySpecialRenderer(MetalPressTileEntity.class, new MetalPressRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(CrusherTileEntity.class, new CrusherRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(SheetmetalTankTileEntity.class, new SheetmetalTankRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(SiloTileEntity.class, new SiloRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(SqueezerTileEntity.class, new SqueezerRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(DieselGeneratorTileEntity.class, new DieselGeneratorRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(BucketWheelTileEntity.class, new BucketWheelRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(ArcFurnaceTileEntity.class, new ArcFurnaceRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(AutoWorkbenchTileEntity.class, new AutoWorkbenchRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(BottlingMachineTileEntity.class, new BottlingMachineRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(MixerTileEntity.class, new MixerRenderer());
		//WOOD
		ClientRegistry.bindTileEntitySpecialRenderer(WatermillTileEntity.class, new WatermillRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(WindmillTileEntity.class, new WindmillRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(ModWorkbenchTileEntity.class, new ModWorkbenchRenderer());
		//STONE
		ClientRegistry.bindTileEntitySpecialRenderer(CoresampleTileEntity.class, new CoresampleRenderer());
		//CLOTH
		ClientRegistry.bindTileEntitySpecialRenderer(ShaderBannerTileEntity.class, new ShaderBannerRenderer());


		/*Colours*/
		for(Item item : IEContent.registeredIEItems)
			if(item instanceof IColouredItem&&((IColouredItem)item).hasCustomItemColours())
				mc().getItemColors().register(IEDefaultColourHandlers.INSTANCE, item);
		for(Block block : IEContent.registeredIEBlocks)
			if(block instanceof IColouredBlock&&((IColouredBlock)block).hasCustomBlockColours())
				mc().getBlockColors().register(IEDefaultColourHandlers.INSTANCE, block);

		/*Render Layers*/
		Map<String, PlayerRenderer> skinMap = Minecraft.getInstance().getRenderManager().getSkinMap();
		PlayerRenderer render = skinMap.get("default");
		render.addLayer(new IEBipedLayerRenderer<>(render));
		render = skinMap.get("slim");
		render.addLayer(new IEBipedLayerRenderer<>(render));
	}

	@Override
	public void initEnd()
	{
		for(IECompatModule compat : IECompatModule.modules)
			try
			{
				compat.clientInit();
			} catch(Exception exception)
			{
				IELogger.error("Compat module for "+compat+" could not be client pre-initialized");
			}
	}

	@Override
	public void postInit()
	{
		ManualHelper.ieManualInstance = new IEManualInstance();

		ManualInstance ieMan = ManualHelper.getManual();
		ieMan.registerSpecialElement(new ResourceLocation(MODID, "blueprint"),
				s -> {
					ItemStack[] stacks;
					if(JSONUtils.isJsonArray(s, "recipes"))
					{
						JsonArray arr = s.get("recipes").getAsJsonArray();
						stacks = new ItemStack[arr.size()];
						for(int i = 0; i < stacks.length; ++i)
							stacks[i] = CraftingHelper.getItemStack(arr.get(i).getAsJsonObject(), true);
					}
					else
					{
						JsonElement recipe = s.get("recipe");
						Preconditions.checkArgument(recipe.isJsonObject());
						stacks = new ItemStack[]{
								CraftingHelper.getItemStack(recipe.getAsJsonObject(), true)
						};
					}
					return new ManualElementBlueprint(ieMan, stacks);
				});
		ieMan.registerSpecialElement(new ResourceLocation(MODID, "multiblock"),
				s -> {
					ResourceLocation name = ManualUtils.getLocationForManual(
							JSONUtils.getString(s, "name"),
							ieMan
					);
					IMultiblock mb = MultiblockHandler.getByUniqueName(name);
					if(mb==null)
						throw new NullPointerException("Multiblock "+name+" does not exist");
					return new ManualElementMultiblock(ieMan, mb);
				});
		InnerNode<ResourceLocation, ManualEntry> energyCat = ieMan.contentTree.getRoot().getOrCreateSubnode(new ResourceLocation(MODID,
				ManualHelper.CAT_ENERGY));
		InnerNode<ResourceLocation, ManualEntry> generalCat = ieMan.contentTree.getRoot().getOrCreateSubnode(new ResourceLocation(MODID,
				ManualHelper.CAT_GENERAL), -1);
		InnerNode<ResourceLocation, ManualEntry> constructionCat = ieMan.contentTree.getRoot().getOrCreateSubnode(new ResourceLocation(MODID,
				ManualHelper.CAT_CONSTRUCTION));
		InnerNode<ResourceLocation, ManualEntry> toolsCat = ieMan.contentTree.getRoot().getOrCreateSubnode(new ResourceLocation(MODID,
				ManualHelper.CAT_TOOLS));
		InnerNode<ResourceLocation, ManualEntry> machinesCat = ieMan.contentTree.getRoot().getOrCreateSubnode(new ResourceLocation(MODID,
				ManualHelper.CAT_MACHINES));
		InnerNode<ResourceLocation, ManualEntry> heavyMachinesCat = ieMan.contentTree.getRoot().getOrCreateSubnode(new ResourceLocation(MODID,
				ManualHelper.CAT_HEAVYMACHINES));

		ieMan.addEntry(energyCat, new ResourceLocation(MODID, "wiring"));
		ieMan.addEntry(energyCat, new ResourceLocation(MODID, "generator"));
		ieMan.addEntry(energyCat, new ResourceLocation(MODID, "breaker"));
		ieMan.addEntry(energyCat, new ResourceLocation(MODID, "current_transformer"));
		ieMan.addEntry(energyCat, new ResourceLocation(MODID, "redstone_wire"));

		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "introduction"), -1);
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "ores"));
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "hemp"));
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "alloys"));
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "components"));
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "plates"));
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "alloykiln"));
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "cokeoven"));
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "crude_blast_furnace"));
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "improved_blast_furnace"));
		ieMan.addEntry(generalCat, new ResourceLocation(MODID, "graphite"));

		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "balloon"));
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "metalconstruction"));
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "concrete"));
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "crate"));
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "barrel"));
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "lighting"));
		ieMan.addEntry(constructionCat, new ResourceLocation(MODID, "treated_wood"));

		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "jerrycan"));
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "mining_drill"));
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "ear_defenders"));
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "shield"));
		ieMan.addEntry(toolsCat, new ResourceLocation(MODID, "toolbox"));

		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "conveyors"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "external_heater"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "item_router"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "fluid_router"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "turntable"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "fluid_transport"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "charging_station"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "garden_cloche"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "tesla_coil"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "razor_wire"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "turrets"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "assembler"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "bottling_machine"));
		ieMan.addEntry(machinesCat, new ResourceLocation(MODID, "automated_workbench"));

		ieMan.addEntry(heavyMachinesCat, new ResourceLocation(MODID, "refinery"));
		ieMan.addEntry(heavyMachinesCat, new ResourceLocation(MODID, "metal_press"));

		//TODO needs to change on world reload
		String[][] table = formatToTable_ItemIntHashmap(ThermoelectricHandler.getThermalValuesSorted(true), "K");
		ManualEntry.ManualEntryBuilder builder = new ManualEntry.ManualEntryBuilder(ManualHelper.getManual());
		builder.addSpecialElement("values", 0, new ManualElementTable(ieMan, table, false));
		builder.readFromFile(new ResourceLocation(MODID, "thermoelectric"));
		ieMan.addEntry(energyCat, builder.create());

		addChangelogToManual();

		//TODO ClientCommandHandler.instance.registerCommand(new CommandHandler(true));
	}

	@Override
	public void postInitEnd()
	{
		for(IECompatModule compat : IECompatModule.modules)
			try
			{
				compat.clientPostInit();
			} catch(Exception exception)
			{
				IELogger.error("Compat module for "+compat+" could not be client pre-initialized");
			}
	}

	static ManualEntry mineralEntry;

	public static void handleMineralManual()
	{
		/*if(ManualHelper.getManual()!=null)
		{
			ArrayList<IManualPage> pages = new ArrayList();
			pages.add(new ManualPages.Text(ManualHelper.getManual(), "minerals0"));
			pages.add(new ManualPages.Crafting(ManualHelper.getManual(), "minerals1", new ItemStack(IEContent.blockMetalDevice1, 1, BlockTypes_MetalDevice1.SAMPLE_DRILL.getMeta())));
			pages.add(new ManualPages.Text(ManualHelper.getManual(), "minerals2"));

			final ExcavatorHandler.MineralMix[] minerals = ExcavatorHandler.mineralList.keySet().toArray(new ExcavatorHandler.MineralMix[0]);

			ArrayList<Integer> mineralIndices = new ArrayList();
			for(int i = 0; i < minerals.length; i++)
				if(minerals[i].isValid())
					mineralIndices.add(i);
			Collections.sort(mineralIndices, new Comparator<Integer>()
			{
				@Override
				public int compare(Integer paramT1, Integer paramT2)
				{
					String name1 = Lib.DESC_INFO+"mineral."+minerals[paramT1].name;
					String localizedName1 = I18n.format(name1);
					if(localizedName1==name1)
						localizedName1 = minerals[paramT1].name;

					String name2 = Lib.DESC_INFO+"mineral."+minerals[paramT2].name;
					String localizedName2 = I18n.format(name2);
					if(localizedName2==name2)
						localizedName2 = minerals[paramT2].name;
					return localizedName1.compareToIgnoreCase(localizedName2);
				}
			});
			for(int i : mineralIndices)
			{
				String name = Lib.DESC_INFO+"mineral."+minerals[i].name;
				String localizedName = I18n.format(name);
				if(localizedName.equalsIgnoreCase(name))
					localizedName = minerals[i].name;

				String s0 = "";
				if(minerals[i].dimensionWhitelist!=null&&minerals[i].dimensionWhitelist.length > 0)
				{
					String validDims = "";
					for(int dim : minerals[i].dimensionWhitelist)
						validDims += (!validDims.isEmpty()?", ": "")+"<dim;"+dim+">";
					s0 = I18n.format("ie.manual.entry.mineralsDimValid", localizedName, validDims);
				}
				else if(minerals[i].dimensionBlacklist!=null&&minerals[i].dimensionBlacklist.length > 0)
				{
					String invalidDims = "";
					for(int dim : minerals[i].dimensionBlacklist)
						invalidDims += (!invalidDims.isEmpty()?", ": "")+"<dim;"+dim+">";
					s0 = I18n.format("ie.manual.entry.mineralsDimInvalid", localizedName, invalidDims);
				}
				else
					s0 = I18n.format("ie.manual.entry.mineralsDimAny", localizedName);

				ArrayList<Integer> formattedOutputs = new ArrayList<Integer>();
				for(int j = 0; j < minerals[i].oreOutput.size(); j++)
					formattedOutputs.add(j);
				final int fi = i;
				Collections.sort(formattedOutputs, new Comparator<Integer>()
				{
					@Override
					public int compare(Integer paramT1, Integer paramT2)
					{
						return -Double.compare(minerals[fi].recalculatedChances[paramT1], minerals[fi].recalculatedChances[paramT2]);
					}
				});

				String s1 = "";
				NonNullList<ItemStack> sortedOres = NonNullList.withSize(minerals[i].oreOutput.size(), ItemStack.EMPTY);
				for(int j = 0; j < formattedOutputs.size(); j++)
					if(!minerals[i].oreOutput.get(j).isEmpty())
					{
						int sorted = formattedOutputs.get(j);
						s1 += "<br>"+new DecimalFormat("00.00").format(minerals[i].recalculatedChances[sorted]*100).replaceAll("\\G0", " ")+"% "+minerals[i].oreOutput.get(sorted).getDisplayName();
						sortedOres.set(j, minerals[i].oreOutput.get(sorted));
					}
				String s2 = I18n.format("ie.manual.entry.minerals3", s0, s1);
				pages.add(new ManualPages.ItemDisplay(ManualHelper.getManual(), s2, sortedOres));
			}

//			String[][][] multiTables = formatToTable_ExcavatorMinerals();
//			for(String[][] minTable : multiTables)
//				pages.add(new ManualPages.Table(ManualHelper.getManual(), "", minTable,true));
			//if(mineralEntry!=null)
			//	mineralEntry.setPages(pages.toArray(new IManualPage[pages.size()]));
			//else
			//{
			//	ManualHelper.addEntry("minerals", ManualHelper.CAT_GENERAL, pages.toArray(new IManualPage[pages.size()]));
			//	mineralEntry = ManualHelper.getManual().getEntry("minerals");
			//}
		}*/
	}

	static String[][][] formatToTable_ExcavatorMinerals()
	{
		ExcavatorHandler.MineralMix[] minerals = ExcavatorHandler.mineralList.keySet().toArray(new ExcavatorHandler.MineralMix[0]);
		String[][][] multiTables = new String[1][minerals.length][2];
		int curTable = 0;
		int totalLines = 0;
		for(int i = 0; i < minerals.length; i++)
			if(minerals[i].isValid())
			{
				String name = Lib.DESC_INFO+"mineral."+minerals[i].name;
				if(I18n.format(name)==name)
					name = minerals[i].name;
				multiTables[curTable][i][0] = name;
				multiTables[curTable][i][1] = "";
				for(int j = 0; j < minerals[i].oreOutput.size(); j++)
					if(!minerals[i].oreOutput.get(j).isEmpty())
					{
						multiTables[curTable][i][1] += minerals[i].oreOutput.get(j).getDisplayName()+" "+(new DecimalFormat("#.00").format(minerals[i].recalculatedChances[j]*100)+"%")+(j < minerals[i].oreOutput.size()-1?"\n": "");
						totalLines++;
					}
				if(i < minerals.length-1&&totalLines+minerals[i+1].oreOutput.size() >= 13)
				{
					String[][][] newMultiTables = new String[multiTables.length+1][minerals.length][2];
					System.arraycopy(multiTables, 0, newMultiTables, 0, multiTables.length);
					multiTables = newMultiTables;
					totalLines = 0;
					curTable++;
				}
			}
		return multiTables;
	}

	public void addChangelogToManual()
	{
		SortedMap<ComparableVersion, ManualEntry> allChanges = new TreeMap<>(Comparator.reverseOrder());
		ComparableVersion currIEVer = new ComparableVersion(ImmersiveEngineering.VERSION);
		//Included changelog
		try(InputStream in = Minecraft.getInstance().getResourceManager().getResource(new ResourceLocation(MODID,
				"changelog.json")).getInputStream())
		{
			JsonElement ele = new JsonParser().parse(new InputStreamReader(in));
			JsonObject upToCurrent = ele.getAsJsonObject();
			for(Entry<String, JsonElement> entry : upToCurrent.entrySet())
			{
				ComparableVersion version = new ComparableVersion(entry.getKey());
				ManualEntry manualEntry = addVersionToManual(currIEVer, version,
						entry.getValue().getAsString(), false);
				if(manualEntry!=null)
					allChanges.put(version, manualEntry);
			}
		} catch(IOException x)
		{
			x.printStackTrace();
		}
		//Changelog from update JSON
		CheckResult result = VersionChecker.getResult(ModLoadingContext.get().getActiveContainer().getModInfo());
		if(result.status!=Status.PENDING&&result.status!=Status.FAILED)
			for(Entry<ComparableVersion, String> e : result.changes.entrySet())
				allChanges.put(e.getKey(), addVersionToManual(currIEVer, e.getKey(), e.getValue(), true));

		ManualInstance ieMan = ManualHelper.getManual();
		InnerNode<ResourceLocation, ManualEntry> updateCat = ieMan.contentTree.getRoot().getOrCreateSubnode(new ResourceLocation(MODID,
				ManualHelper.CAT_UPDATE), -2);
		for(ManualEntry entry : allChanges.values())
			ManualHelper.getManual().addEntry(updateCat, entry);
	}

	private ManualEntry addVersionToManual(ComparableVersion currVer, ComparableVersion version, String changes, boolean ahead)
	{
		String title = version.toString();
		if(ahead)
			title += I18n.format("ie.manual.newerVersion");
		else
		{
			int cmp = currVer.compareTo(version);
			if(cmp==0)
				title += I18n.format("ie.manual.currentVersion");
			//TODO what are these supposed to do???

			//else if(cmp < 0)
			//	return null;
		}

		String text = changes.replace("\t", "  ");
		ManualEntry.ManualEntryBuilder builder = new ManualEntryBuilder(ManualHelper.getManual());
		builder.setContent(title, "", text);
		builder.setLocation(new ResourceLocation(MODID, "changelog_"+version.toString()));
		return builder.create();
	}

	@Override
	public void serverStarting()
	{
	}

	//TODO are these here rather than in ClientEventHandler for any particular reason???
	@SubscribeEvent
	public static void textureStichPre(TextureStitchEvent.Pre event)
	{
		if(event.getMap()!=mc().getTextureMap())
			return;
		IELogger.info("Stitching Revolver Textures!");
		RevolverItem.addRevolverTextures(event);
		for(ShaderRegistry.ShaderRegistryEntry entry : ShaderRegistry.shaderRegistry.values())
			for(ShaderCase sCase : entry.getCases())
				if(sCase.stitchIntoSheet())
					for(ShaderLayer layer : sCase.getLayers())
						if(layer.getTexture()!=null)
							event.addSprite(layer.getTexture());

		for(DrillHeadPerm p : DrillHeadPerm.ALL_PERMS)
			event.addSprite(p.texture);
		event.addSprite(new ResourceLocation(MODID, "block/wire"));
		event.addSprite(new ResourceLocation(MODID, "block/shaders/greyscale_fire"));

		//TODO this shouldn't be necessary any more
		for(BulletHandler.IBullet bullet : BulletHandler.getAllValues())
			for(ResourceLocation rl : bullet.getTextures())
				event.addSprite(rl);

		for(ResourceLocation rl : ModelConveyor.rl_casing)
			event.addSprite(rl);
		event.addSprite(ConveyorHandler.textureConveyorColour);
		event.addSprite(BasicConveyor.texture_off);
		event.addSprite(BasicConveyor.texture_on);
		event.addSprite(DropConveyor.texture_off);
		event.addSprite(DropConveyor.texture_on);
		event.addSprite(VerticalConveyor.texture_off);
		event.addSprite(VerticalConveyor.texture_on);
		event.addSprite(SplitConveyor.texture_off);
		event.addSprite(SplitConveyor.texture_on);
		event.addSprite(SplitConveyor.texture_casing);

		event.addSprite(new ResourceLocation(MODID, "block/fluid/creosote_still"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/creosote_flow"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/plantoil_still"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/plantoil_flow"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/ethanol_still"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/ethanol_flow"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/biodiesel_still"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/biodiesel_flow"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/concrete_still"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/concrete_flow"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/potion_still"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/potion_flow"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/hot_metal_still"));
		event.addSprite(new ResourceLocation(MODID, "block/fluid/hot_metal_flow"));

		event.addSprite(new ResourceLocation(MODID, "item/shader_slot"));
	}

	@SubscribeEvent
	public static void textureStichPost(TextureStitchEvent.Post event)
	{
		if(event.getMap()!=mc().getTextureMap())
			return;
		ImmersiveEngineering.proxy.clearRenderCaches();
		RevolverItem.retrieveRevolverTextures(event.getMap());
		for(DrillHeadPerm p : DrillHeadPerm.ALL_PERMS)
		{
			p.sprite = event.getMap().getSprite(p.texture);
			Preconditions.checkNotNull(p.sprite);
		}
		WireType.iconDefaultWire = event.getMap().getSprite(new ResourceLocation(MODID, "block/wire"));
		AtlasTexture texturemap = Minecraft.getInstance().getTextureMap();
		for(int i = 0; i < ClientUtils.destroyBlockIcons.length; i++)
		{
			ClientUtils.destroyBlockIcons[i] = texturemap.getSprite(new ResourceLocation("block/destroy_stage_"+i));
			Preconditions.checkNotNull(ClientUtils.destroyBlockIcons[i]);
		}
	}

	public void registerItemModel(Item item, String path, String renderCase)
	{
		Minecraft.getInstance().getItemRenderer().getItemModelMesher().register(item, new ModelResourceLocation(path, renderCase));
	}

	public static String getPropertyString(Map<IProperty, Comparable> propertyMap)
	{
		StringBuilder stringbuilder = new StringBuilder();
		for(Entry<IProperty, Comparable> entry : propertyMap.entrySet())
		{
			if(stringbuilder.length()!=0)
				stringbuilder.append(",");
			IProperty iproperty = entry.getKey();
			Comparable comparable = entry.getValue();
			stringbuilder.append(iproperty.getName());
			stringbuilder.append("=");
			stringbuilder.append(iproperty.getName(comparable));
		}
		if(stringbuilder.length()==0)
			stringbuilder.append("normal");
		return stringbuilder.toString();
	}


	HashMap<String, IETileSound> soundMap = new HashMap<String, IETileSound>();
	HashMap<BlockPos, IETileSound> tileSoundMap = new HashMap<BlockPos, IETileSound>();

	@Override
	public void handleTileSound(SoundEvent soundEvent, TileEntity tile, boolean tileActive, float volume, float pitch)
	{
		BlockPos pos = tile.getPos();
		IETileSound sound = tileSoundMap.get(pos);
		if(sound==null&&tileActive)
		{
			sound = ClientUtils.generatePositionedIESound(soundEvent, volume, pitch, true, 0, pos);
			tileSoundMap.put(pos, sound);
		}
		else if(sound!=null&&!tileActive)
		{
			sound.donePlaying = true;
			mc().getSoundHandler().stop(sound);
			tileSoundMap.remove(pos);
		}
	}

	@Override
	public void stopTileSound(String soundName, TileEntity tile)
	{
		IETileSound sound = soundMap.get(soundName);
		if(sound!=null&&new BlockPos(sound.getX(), sound.getY(), sound.getZ()).equals(tile.getPos()))
			mc().getSoundHandler().stop(sound);
	}

	@Override
	public void onWorldLoad()
	{
		/*TODO
		if(!ShaderMinecartModel.rendersReplaced)
		{
			for(Object render : mc().getRenderManager().renderers.values())
				if(MinecartRenderer.class.isAssignableFrom(render.getClass()))
				{
					Object wrapped = ObfuscationReflectionHelper.getPrivateValue(MinecartRenderer.class, (MinecartRenderer)render, "field_77013_a");//modelMinecart
					if(wrapped instanceof MinecartModel)
						ObfuscationReflectionHelper.setPrivateValue(MinecartRenderer.class, (MinecartRenderer)render,
								new ShaderMinecartModel((MinecartModel)wrapped), "field_77013_a");//modelMinecart
				}
			ShaderMinecartModel.rendersReplaced = true;
		}
		if(!IEBipedLayerRenderer.rendersAssigned)
		{
			for(Object render : mc().getRenderManager().renderers.values())
				if(BipedRenderer.class.isAssignableFrom(render.getClass()))
					((BipedRenderer)render).addLayer(new IEBipedLayerRenderer<>((BipedRenderer)render));
				else if(ArmorStandRenderer.class.isAssignableFrom(render.getClass()))
					((ArmorStandRenderer)render).addLayer(new IEBipedLayerRenderer<>((ArmorStandRenderer)render));
			IEBipedLayerRenderer.rendersAssigned = true;
		}*/
	}

	@Override
	public void spawnBucketWheelFX(BucketWheelTileEntity tile, ItemStack stack)
	{
		if(stack!=null&&IEConfig.MACHINES.excavator_particles.get())
		{
			Direction facing = tile.getFacing();
			for(int i = 0; i < 16; i++)
			{
				double x = tile.getPos().getX()+.5;
				if(facing.getAxis()==Axis.Z)
					x += .1*(2*(tile.getWorldNonnull().rand.nextDouble()-.5));
				else
					x -= .5*facing.getAxisDirection().getOffset();
				double y = tile.getPos().getY()+2.5;
				double z = tile.getPos().getZ()+.5+.1*0;
				if(tile.getFacing().getAxis()==Axis.X)
					z += .1*(2*(tile.getWorldNonnull().rand.nextGaussian()-.5));
				else
					z -= .5*facing.getAxisDirection().getOffset();
				double mX = (tile.getWorldNonnull().rand.nextDouble()-.5)*.01;
				;
				if(facing.getAxis()==Axis.X)
				{
					int sign = (tile.getIsMirrored()^facing.getAxisDirection()==AxisDirection.NEGATIVE)?1: -1;
					mX += .075*sign;
				}
				double mY = tile.getWorld().rand.nextDouble()*-0.05D;
				double mZ = (tile.getWorldNonnull().rand.nextDouble()-.5)*.01;
				;
				if(facing.getAxis()==Axis.Z)
				{
					int sign = (tile.getIsMirrored()^facing.getAxisDirection()==AxisDirection.NEGATIVE)?1: -1;
					mZ += .075*sign;
				}

				Particle particle = new BreakingParticle.Factory().makeParticle(new ItemParticleData(ParticleTypes.ITEM, stack),
						tile.getWorldNonnull(), x, y, z, mX, mY, mZ);
				mc().particles.addEffect(particle);
			}
		}
	}

	//TODO move to commonProxy or even use directly
	@Override
	public void spawnRedstoneFX(World world, double x, double y, double z, double mx, double my, double mz, float size, float r, float g, float b)
	{
		world.addParticle(new RedstoneParticleData(r, g, b, size), x, y, z, mx, my, mz);
	}

	@Override
	public void spawnFluidSplashFX(World world, FluidStack fs, double x, double y, double z, double mx, double my, double mz)
	{
		world.addParticle(new Data(fs.getFluid()), x, y, z, mx, my, mz);
	}

	@Override
	public void spawnBubbleFX(World world, FluidStack fs, double x, double y, double z, double mx, double my, double mz)
	{
		world.addParticle(IEParticles.IE_BUBBLE, x, y, z, mx, my, mz);
	}

	@Override
	public void spawnFractalFX(World world, double x, double y, double z, Vec3d direction, double scale, int prefixColour, float[][] colour)
	{
		if(prefixColour >= 0)
			colour = prefixColour==1?FractalParticle.COLOUR_ORANGE: prefixColour==2?FractalParticle.COLOUR_RED: FractalParticle.COLOUR_LIGHTNING;
		FractalParticle.Data particle = new FractalParticle.Data(direction, scale, 10, 16, colour[0], colour[1]);
		world.addParticle(particle, x, y, z, 0, 0, 0);
	}

	@Override
	public void draw3DBlockCauldron()
	{
		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockState state = Blocks.CAULDRON.getDefaultState();
		IBakedModel model = blockRenderer.getBlockModelShapes().getModel(state);

		renderBlockModel(blockRenderer, model, state);
	}

	@Override
	public void drawSpecificFluidPipe(String configuration)
	{
		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockState state = MetalDevices.fluidPipe.getDefaultState();
		IBakedModel model = blockRenderer.getBlockModelShapes().getModel(state);
		GlStateManager.pushMatrix();
		GlStateManager.translated(0, 0, 1);
		renderBlockModel(blockRenderer, model, state);
		GlStateManager.popMatrix();
	}

	private void renderBlockModel(BlockRendererDispatcher blockRenderer, IBakedModel model, BlockState state)
	{
		RenderHelper.disableStandardItemLighting();
		GlStateManager.blendFunc(770, 771);
		GlStateManager.enableBlend();
		GlStateManager.disableCull();
		if(Minecraft.isAmbientOcclusionEnabled())
			GlStateManager.shadeModel(7425);
		else
			GlStateManager.shadeModel(7424);
		blockRenderer.getBlockModelRenderer().renderModelBrightness(model, state, .75f, false);
	}

	static Map<String, Boolean> hasArmorModel = new HashMap<>();

	@Override
	public boolean armorHasCustomModel(ItemStack stack)
	{
		if(!stack.isEmpty()&&stack.getItem() instanceof ArmorItem)
		{
			Boolean b = hasArmorModel.get(stack.getTranslationKey());
			if(b==null)
				try
				{
					BipedModel<?> model = stack.getItem().getArmorModel(mc().player, stack, ((ArmorItem)stack.getItem()).getEquipmentSlot(), null);
					b = model!=null&&model.getClass()!=BipedModel.class; //Model isn't a base Biped
					hasArmorModel.put(stack.getTranslationKey(), b);
				} catch(Exception e)
				{
				}
			return b==null?false: b;
		}
		return false;
	}

	@Override
	public boolean drawConveyorInGui(String conveyor, Direction facing)
	{
		IConveyorBelt con = ConveyorHandler.getConveyor(new ResourceLocation(conveyor), null);
		if(con!=null)
		{
			GlStateManager.pushMatrix();
			List<BakedQuad> quads = ModelConveyor.getBaseConveyor(facing, 1, new Matrix4(facing), ConveyorDirection.HORIZONTAL,
					ClientUtils.getSprite(con.getActiveTexture()), new boolean[]{true, true}, new boolean[]{true, true}, null, DyeColor.WHITE);
//			GlStateManager.translate(0, 0, 1);
			ClientUtils.renderQuads(quads, 1, 1, 1, 1);
			GlStateManager.popMatrix();
			return true;
		}
		return false;
	}

	@Override
	public void drawFluidPumpTop()
	{
		final BlockRendererDispatcher blockRenderer = Minecraft.getInstance().getBlockRendererDispatcher();
		BlockState state = MetalDevices.fluidPump.getDefaultState();
		state = state.with(IEProperties.MULTIBLOCKSLAVE, true);
		IBakedModel model = blockRenderer.getBlockModelShapes().getModel(state);

		GlStateManager.pushMatrix();
		GlStateManager.translated(0, 0, 1);
		renderBlockModel(blockRenderer, model, state);
		GlStateManager.popMatrix();
	}

	static <T> String[][] formatToTable_ItemIntHashmap(Map<T, Integer> map, String valueType)
	{
		List<Entry<T, Integer>> sortedMapArray = new ArrayList<>(map.entrySet());
		sortedMapArray.sort(Comparator.comparing(Entry::getValue));
		ArrayList<String[]> list = new ArrayList<>();
		try
		{
			for(Entry<T, Integer> entry : sortedMapArray)
			{
				String item = entry.getKey().toString();
				if(entry.getKey() instanceof ResourceLocation)
				{
					ResourceLocation key = (ResourceLocation)entry.getKey();
					if(ApiUtils.isNonemptyItemTag(key))
					{
						ItemStack is = IEApi.getPreferredTagStack(key);
						if(!is.isEmpty())
							item = is.getDisplayName().getFormattedText();
					}
				}

				if(item!=null)
				{
					int bt = entry.getValue();
					String am = bt+" "+valueType;
					list.add(new String[]{item, am});
				}
			}
		} catch(Exception e)
		{
		}
		return list.toArray(new String[0][]);
	}


	@Override
	public String[] splitStringOnWidth(String s, int w)
	{
		return ClientUtils.font().listFormattedStringToWidth(s, w).toArray(new String[0]);
	}

	@Override
	public World getClientWorld()
	{
		return mc().world;
	}

	@Override
	public PlayerEntity getClientPlayer()
	{
		return mc().player;
	}

	@Override
	public String getNameFromUUID(String uuid)
	{
		return mc().getSessionService().fillProfileProperties(new GameProfile(UUID.fromString(uuid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")), null), false).getName();
	}

	@Override
	public void reInitGui()
	{
		Screen currentScreen = mc().currentScreen;
		if(currentScreen instanceof IEContainerScreen)
			currentScreen.init(mc(), currentScreen.width, currentScreen.height);
	}

	@Override
	public void removeStateFromSmartModelCache(BlockState state)
	{
		for(BlockRenderLayer r : BlockRenderLayer.values())
			IESmartObjModel.modelCache.remove(new RenderCacheKey(state, r));
		IESmartObjModel.modelCache.remove(new RenderCacheKey(state, null));
	}

	@Override
	public void removeStateFromConnectionModelCache(BlockState state)
	{
		//TODO
		for(BlockRenderLayer r : BlockRenderLayer.values())
			BakedConnectionModel.cache.invalidate(new RenderCacheKey(state, r));
		BakedConnectionModel.cache.invalidate(new RenderCacheKey(state, null));
	}

	@Override
	public void clearConnectionModelCache()
	{
		BakedConnectionModel.cache.invalidateAll();
	}

	@Override
	public void reloadManual()
	{
		if(ManualHelper.getManual()!=null)
			ManualHelper.getManual().reload();
	}

	static
	{
		IEApi.renderCacheClearers.add(IESmartObjModel.modelCache::clear);
		IEApi.renderCacheClearers.add(IESmartObjModel.cachedBakedItemModels::invalidateAll);
		IEApi.renderCacheClearers.add(BakedConnectionModel.cache::invalidateAll);
		IEApi.renderCacheClearers.add(ModelConveyor.modelCache::clear);
		IEApi.renderCacheClearers.add(ModelConfigurableSides.modelCache::invalidateAll);
		IEApi.renderCacheClearers.add(FluidPipeTileEntity.cachedOBJStates::clear);
		IEApi.renderCacheClearers.add(BelljarRenderer::reset);
		IEApi.renderCacheClearers.add(WatermillRenderer::reset);
		IEApi.renderCacheClearers.add(WindmillRenderer::reset);
		IEApi.renderCacheClearers.add(ModelCoresample::clearCache);
		IEApi.renderCacheClearers.add(ModelItemDynamicOverride.modelCache::clear);
		IEApi.renderCacheClearers.add(ModelPowerpack.catenaryCacheLeft::invalidateAll);
		IEApi.renderCacheClearers.add(ModelPowerpack.catenaryCacheRight::invalidateAll);
		IEApi.renderCacheClearers.add(FeedthroughModel.CACHE::invalidateAll);
	}

	@Override
	public void clearRenderCaches()
	{
		for(Runnable r : IEApi.renderCacheClearers)
			r.run();
	}

	@Override
	public void startSkyhookSound(SkylineHookEntity hook)
	{
		Minecraft.getInstance().getSoundHandler().play(new SkyhookSound(hook,
				new ResourceLocation(MODID, "skyhook")));
	}

	@Override
	public void openManual()
	{
		Minecraft.getInstance().displayGuiScreen(ManualHelper.getManual().getGui());
	}

	@Override
	public void registerContainersAndScreens()
	{
		super.registerContainersAndScreens();
		registerScreen(Lib.GUIID_CokeOven, CokeOvenScreen::new);
		registerScreen(Lib.GUIID_AlloySmelter, AlloySmelterScreen::new);
		registerScreen(Lib.GUIID_BlastFurnace, BlastFurnaceScreen::new);
		registerScreen(Lib.GUIID_WoodenCrate, CrateScreen::new);
		registerScreen(Lib.GUIID_Workbench, ModWorkbenchScreen::new);
		registerScreen(Lib.GUIID_Assembler, AssemblerScreen::new);
		registerScreen(Lib.GUIID_Sorter, SorterScreen::new);
		registerScreen(Lib.GUIID_Squeezer, SqueezerScreen::new);
		registerScreen(Lib.GUIID_Fermenter, FermenterScreen::new);
		registerScreen(Lib.GUIID_Refinery, RefineryScreen::new);
		registerScreen(Lib.GUIID_ArcFurnace, ArcFurnaceScreen::new);
		registerScreen(Lib.GUIID_AutoWorkbench, AutoWorkbenchScreen::new);
		registerScreen(Lib.GUIID_Mixer, MixerScreen::new);
		registerScreen(Lib.GUIID_Turret_Gun, GunTurretScreen::new);
		registerScreen(Lib.GUIID_Turret_Chem, ChemTurretScreen::new);
		registerScreen(Lib.GUIID_FluidSorter, FluidSorterScreen::new);
		registerScreen(Lib.GUIID_Belljar, BelljarScreen::new);
		registerScreen(Lib.GUIID_ToolboxBlock, ToolboxBlockScreen::new);

		registerScreen(Lib.GUIID_Toolbox, ToolboxScreen::new);
		registerScreen(Lib.GUIID_Revolver, RevolverScreen::new);
		registerScreen(Lib.GUIID_MaintenanceKit, MaintenanceKitScreen::new);
	}


	public <C extends Container, S extends Screen & IHasContainer<C>>
	void registerScreen(ResourceLocation containerName, IScreenFactory<C, S> factory)
	{
		ContainerType<C> type = (ContainerType<C>)GuiHandler.getContainerType(containerName);
		ScreenManager.registerFactory(type, factory);
	}

	@Override
	public Item.Properties useIEOBJRenderer(Item.Properties props)
	{
		return super.useIEOBJRenderer(props).setTEISR(() -> () -> IEOBJItemRenderer.INSTANCE);
	}
}
