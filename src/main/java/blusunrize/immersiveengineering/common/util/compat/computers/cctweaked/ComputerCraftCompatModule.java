/*
 * BluSunrize
 * Copyright (c) 2020
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 *
 */

package blusunrize.immersiveengineering.common.util.compat.computers.cctweaked;

import blusunrize.immersiveengineering.ImmersiveEngineering;
import blusunrize.immersiveengineering.api.utils.CapabilityUtils;
import blusunrize.immersiveengineering.common.blocks.IEBlocks.Connectors;
import blusunrize.immersiveengineering.common.blocks.metal.ConnectorBundledTileEntity;
import blusunrize.immersiveengineering.common.util.compat.IECompatModule;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.CallbackOwner;
import blusunrize.immersiveengineering.common.util.compat.computers.generic.Callbacks;
import dan200.computercraft.api.ComputerCraftAPI;
import dan200.computercraft.api.peripheral.IPeripheral;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.LazyValue;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ComputerCraftCompatModule extends IECompatModule
{
	@CapabilityInject(IPeripheral.class)
	public static Capability<IPeripheral> PERIPHERAL_CAPABILITY;

	private final Map<TileEntityType<?>, PeripheralCreator<?>> knownPeripherals = new HashMap<>();

	@Override
	public void preInit()
	{
	}

	@Override
	public void registerRecipes()
	{
	}

	@Override
	public void init()
	{
		ComputerCraftAPI.registerBundledRedstoneProvider((world, pos, direction) -> {
			final int doNotHandle = -1;
			BlockState state = world.getBlockState(pos);
			if(state.getBlock()!=Connectors.connectorBundled)
				return doNotHandle;
			TileEntity tile = world.getTileEntity(pos);
			if(!(tile instanceof ConnectorBundledTileEntity))
				return doNotHandle;
			int bits = 0;
			for(int color = 0; color < 16; ++color)
				if(((ConnectorBundledTileEntity)tile).getValue(color) > 0)
					bits |= 1<<color;
			return bits;
		});

		ConnectorBundledTileEntity.EXTRA_SOURCES.add((world, emittingBlock, emittingSide) -> {
			int output = ComputerCraftAPI.getBundledRedstoneOutput(world, emittingBlock, emittingSide);
			if(output==0||output==-1)
			{
				return null;
			}
			byte[] channelValues = new byte[16];
			for(int color = 0; color < 16; ++color)
				channelValues[color] = (byte)(15*((output >> color)&1));
			return channelValues;
		});

		MinecraftForge.EVENT_BUS.addGenericListener(TileEntity.class, this::attachPeripheral);
		try
		{
			for(Entry<TileEntityType<?>, CallbackOwner<?>> entry : Callbacks.getCallbacks().entrySet())
				knownPeripherals.put(entry.getKey(), new PeripheralCreator<>(entry.getValue()));
		} catch(IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void postInit()
	{
	}

	private static final ResourceLocation CAP_NAME = ImmersiveEngineering.rl("cc_peripheral");

	private void attachPeripheral(AttachCapabilitiesEvent<TileEntity> ev)
	{
		if(PERIPHERAL_CAPABILITY==null)
			return;
		TileEntity te = ev.getObject();
		PeripheralCreator<?> creator = knownPeripherals.get(te.getType());
		if(creator!=null)
		{
			ev.addCapability(CAP_NAME, new ICapabilityProvider()
			{
				private final LazyValue<LazyOptional<IPeripheral>> realPeripheral = new LazyValue<>(
						() -> {
							IPeripheral peripheral = creator.make(te);
							if(peripheral!=null)
								return CapabilityUtils.constantOptional(peripheral);
							else
								return LazyOptional.<IPeripheral>empty();
						}
				);

				@Nonnull
				@Override
				public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side)
				{
					if(cap==PERIPHERAL_CAPABILITY)
						return realPeripheral.getValue().cast();
					else
						return LazyOptional.empty();
				}
			});
		}
	}
}
