package thaumcraft.common.tiles;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.gamerforea.eventhelper.util.EventUtils;
import com.gamerforea.thaumcraft.ModUtils;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import thaumcraft.api.ThaumcraftApiHelper;
import thaumcraft.api.TileThaumcraft;
import thaumcraft.api.WorldCoordinates;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.nodes.INode;
import thaumcraft.api.nodes.NodeModifier;
import thaumcraft.api.nodes.NodeType;
import thaumcraft.api.research.ScanResult;
import thaumcraft.api.wands.IWandable;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.blocks.BlockTaintFibres;
import thaumcraft.common.config.Config;
import thaumcraft.common.config.ConfigBlocks;
import thaumcraft.common.entities.EntityAspectOrb;
import thaumcraft.common.entities.monster.EntityGiantBrainyZombie;
import thaumcraft.common.items.ItemCompassStone;
import thaumcraft.common.items.wands.ItemWandCasting;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.fx.PacketFXBlockZap;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.lib.research.ScanManager;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.lib.utils.Utils;
import thaumcraft.common.lib.world.ThaumcraftWorldGenerator;

public class TileNode extends TileThaumcraft implements INode, IWandable
{
	long lastActive = 0L;
	AspectList aspects = new AspectList();
	AspectList aspectsBase = new AspectList();
	public static HashMap<String, ArrayList<Integer>> locations = new HashMap();
	private NodeType nodeType;
	private NodeModifier nodeModifier;
	int count;
	int regeneration;
	int wait;
	String id;
	byte nodeLock;
	boolean catchUp;
	public Entity drainEntity;
	public MovingObjectPosition drainCollision;
	public int drainColor;
	public Color targetColor;
	public Color color;

	public TileNode()
	{
		this.nodeType = NodeType.NORMAL;
		this.nodeModifier = null;
		this.count = 0;
		this.regeneration = -1;
		this.wait = 0;
		this.id = null;
		this.nodeLock = 0;
		this.catchUp = false;
		this.drainEntity = null;
		this.drainCollision = null;
		this.drainColor = 16777215;
		this.targetColor = new Color(16777215);
		this.color = new Color(16777215);
	}

	@Override
	public String getId()
	{
		if (this.id == null)
			this.id = this.generateId();

		return this.id;
	}

	public String generateId()
	{
		this.id = this.worldObj.provider.dimensionId + ":" + this.xCoord + ":" + this.yCoord + ":" + this.zCoord;
		if (this.worldObj != null && locations != null)
		{
			ArrayList t = new ArrayList();
			t.add(Integer.valueOf(this.worldObj.provider.dimensionId));
			t.add(Integer.valueOf(this.xCoord));
			t.add(Integer.valueOf(this.yCoord));
			t.add(Integer.valueOf(this.zCoord));
			locations.put(this.id, t);
		}

		return this.id;
	}

	@Override
	public void onChunkUnload()
	{
		if (locations != null)
			locations.remove(this.id);

		super.onChunkUnload();
	}

	@Override
	public void validate()
	{
		super.validate();
	}

	@Override
	public void updateEntity()
	{
		super.updateEntity();
		if (this.id == null)
			this.generateId();

		boolean change = false;
		change = this.handleHungryNodeFirst(change);
		++this.count;
		this.checkLock();
		if (!this.worldObj.isRemote)
		{
			change = this.handleDischarge(change);
			change = this.handleRecharge(change);
			change = this.handleTaintNode(change);
			change = this.handleNodeStability(change);
			change = this.handleDarkNode(change);
			change = this.handlePureNode(change);
			change = this.handleHungryNodeSecond(change);
			if (change)
			{
				this.markDirty();
				this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
			}
		}
		else if (this.getNodeType() == NodeType.DARK && this.count % 50 == 0)
			ItemCompassStone.sinisterNodes.put(new WorldCoordinates(this), Long.valueOf(System.currentTimeMillis()));

	}

	public void nodeChange()
	{
		this.regeneration = -1;
		this.markDirty();
		this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
	}

	@Override
	public boolean canUpdate()
	{
		return true;
	}

	public double getDistanceTo(double par1, double par3, double par5)
	{
		double var7 = this.xCoord + 0.5D - par1;
		double var9 = this.yCoord + 0.5D - par3;
		double var11 = this.zCoord + 0.5D - par5;
		return var7 * var7 + var9 * var9 + var11 * var11;
	}

	@Override
	public int onWandRightClick(World world, ItemStack wandstack, EntityPlayer player, int x, int y, int z, int side, int md)
	{
		return -1;
	}

	@Override
	public ItemStack onWandRightClick(World world, ItemStack wandstack, EntityPlayer player)
	{
		player.setItemInUse(wandstack, Integer.MAX_VALUE);
		ItemWandCasting wand = (ItemWandCasting) wandstack.getItem();
		wand.setObjectInUse(wandstack, this.xCoord, this.yCoord, this.zCoord);
		return wandstack;
	}

	@Override
	public AspectList getAspects()
	{
		return this.aspects;
	}

	@Override
	public AspectList getAspectsBase()
	{
		return this.aspectsBase;
	}

	@Override
	public void setAspects(AspectList aspects)
	{
		this.aspects = aspects;
		this.aspectsBase = aspects.copy();
	}

	@Override
	public int addToContainer(Aspect aspect, int amount)
	{
		int left = amount + this.aspects.getAmount(aspect) - this.aspectsBase.getAmount(aspect);
		left = left > 0 ? left : 0;
		this.aspects.add(aspect, amount - left);
		return left;
	}

	@Override
	public boolean takeFromContainer(Aspect aspect, int amount)
	{
		return this.aspects.reduce(aspect, amount);
	}

	public Aspect takeRandomPrimalFromSource()
	{
		Aspect[] primals = this.aspects.getPrimalAspects();
		Aspect asp = primals[this.worldObj.rand.nextInt(primals.length)];
		return asp != null && this.aspects.reduce(asp, 1) ? asp : null;
	}

	public Aspect chooseRandomFilteredFromSource(AspectList filter, boolean preserve)
	{
		int min = preserve ? 1 : 0;
		ArrayList validaspects = new ArrayList();
		Aspect[] asp = this.aspects.getAspects();
		int len$ = asp.length;

		for (int i$ = 0; i$ < len$; ++i$)
		{
			Aspect prim = asp[i$];
			if (filter.getAmount(prim) > 0 && this.aspects.getAmount(prim) > min)
				validaspects.add(prim);
		}

		if (validaspects.size() == 0)
			return null;
		else
		{
			Aspect var9 = (Aspect) validaspects.get(this.worldObj.rand.nextInt(validaspects.size()));
			if (var9 != null && this.aspects.getAmount(var9) > min)
				return var9;
			else
				return null;
		}
	}

	@Override
	public NodeType getNodeType()
	{
		return this.nodeType;
	}

	@Override
	public void setNodeType(NodeType nodeType)
	{
		this.nodeType = nodeType;
	}

	@Override
	public void setNodeModifier(NodeModifier nodeModifier)
	{
		this.nodeModifier = nodeModifier;
	}

	@Override
	public NodeModifier getNodeModifier()
	{
		return this.nodeModifier;
	}

	@Override
	public int getNodeVisBase(Aspect aspect)
	{
		return this.aspectsBase.getAmount(aspect);
	}

	@Override
	public void setNodeVisBase(Aspect aspect, short nodeVisBase)
	{
		if (this.aspectsBase.getAmount(aspect) < nodeVisBase)
			this.aspectsBase.merge(aspect, nodeVisBase);
		else
			this.aspectsBase.reduce(aspect, this.aspectsBase.getAmount(aspect) - nodeVisBase);

	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		this.lastActive = nbttagcompound.getLong("lastActive");
		AspectList al = new AspectList();
		NBTTagList tlist = nbttagcompound.getTagList("AspectsBase", 10);

		for (int oldBase = 0; oldBase < tlist.tagCount(); ++oldBase)
		{
			NBTTagCompound regen = tlist.getCompoundTagAt(oldBase);
			if (regen.hasKey("key"))
				al.add(Aspect.getAspect(regen.getString("key")), regen.getInteger("amount"));
		}

		Short var9 = Short.valueOf(nbttagcompound.getShort("nodeVisBase"));
		this.aspectsBase = new AspectList();
		if (var9.shortValue() > 0 && al.size() == 0)
		{
			Aspect[] var10 = this.aspects.getAspects();
			int ct = var10.length;

			for (int i$ = 0; i$ < ct; ++i$)
			{
				Aspect inc = var10[i$];
				this.aspectsBase.merge(inc, var9.shortValue());
			}
		}
		else
			this.aspectsBase = al.copy();

		short var11 = 600;
		if (this.getNodeModifier() != null)
			switch (TileNode.SyntheticClass_1.$SwitchMap$thaumcraft$api$nodes$NodeModifier[this.getNodeModifier().ordinal()])
			{
				case 1:
					var11 = 400;
					break;
				case 2:
					var11 = 900;
					break;
				case 3:
					var11 = 0;
			}

		long var12 = System.currentTimeMillis();
		int var13 = var11 * 75;
		if (var11 > 0 && this.lastActive > 0L && var12 > this.lastActive + var13)
			this.catchUp = true;

	}

	@Override
	public void writeToNBT(NBTTagCompound nbttagcompound)
	{
		super.writeToNBT(nbttagcompound);
		nbttagcompound.setLong("lastActive", this.lastActive);
		NBTTagList tlist = new NBTTagList();
		nbttagcompound.setTag("AspectsBase", tlist);
		Aspect[] arr$ = this.aspectsBase.getAspects();
		int len$ = arr$.length;

		for (int i$ = 0; i$ < len$; ++i$)
		{
			Aspect aspect = arr$[i$];
			if (aspect != null)
			{
				NBTTagCompound f = new NBTTagCompound();
				f.setString("key", aspect.getTag());
				f.setInteger("amount", this.aspectsBase.getAmount(aspect));
				tlist.appendTag(f);
			}
		}

	}

	@Override
	public void readCustomNBT(NBTTagCompound nbttagcompound)
	{
		this.id = nbttagcompound.getString("nodeId");
		if (this.worldObj != null && locations != null)
		{
			ArrayList mod = new ArrayList();
			mod.add(Integer.valueOf(this.worldObj.provider.dimensionId));
			mod.add(Integer.valueOf(this.xCoord));
			mod.add(Integer.valueOf(this.yCoord));
			mod.add(Integer.valueOf(this.zCoord));
			locations.put(this.id, mod);
		}

		this.setNodeType(NodeType.values()[nbttagcompound.getByte("type")]);
		byte mod1 = nbttagcompound.getByte("modifier");
		if (mod1 >= 0)
			this.setNodeModifier(NodeModifier.values()[mod1]);
		else
			this.setNodeModifier((NodeModifier) null);

		this.aspects.readFromNBT(nbttagcompound);
		String de = nbttagcompound.getString("drainer");
		if (de != null && de.length() > 0 && this.getWorldObj() != null)
		{
			this.drainEntity = this.getWorldObj().getPlayerEntityByName(de);
			if (this.drainEntity != null)
				this.drainCollision = new MovingObjectPosition(this.xCoord, this.yCoord, this.zCoord, 0, Vec3.createVectorHelper(this.drainEntity.posX, this.drainEntity.posY, this.drainEntity.posZ));
		}

		this.drainColor = nbttagcompound.getInteger("draincolor");
	}

	@Override
	public void writeCustomNBT(NBTTagCompound nbttagcompound)
	{
		if (this.id == null)
			this.id = this.generateId();

		if (this.worldObj != null && locations != null)
		{
			ArrayList t = new ArrayList();
			t.add(Integer.valueOf(this.worldObj.provider.dimensionId));
			t.add(Integer.valueOf(this.xCoord));
			t.add(Integer.valueOf(this.yCoord));
			t.add(Integer.valueOf(this.zCoord));
			locations.put(this.id, t);
		}

		nbttagcompound.setString("nodeId", this.id);
		nbttagcompound.setByte("type", (byte) this.getNodeType().ordinal());
		nbttagcompound.setByte("modifier", this.getNodeModifier() == null ? -1 : (byte) this.getNodeModifier().ordinal());
		this.aspects.writeToNBT(nbttagcompound);
		if (this.drainEntity != null && this.drainEntity instanceof EntityPlayer)
			nbttagcompound.setString("drainer", this.drainEntity.getCommandSenderName());

		nbttagcompound.setInteger("draincolor", this.drainColor);
	}

	@Override
	public void onUsingWandTick(ItemStack wandstack, EntityPlayer player, int count)
	{
		boolean mfu = false;
		ItemWandCasting wand = (ItemWandCasting) wandstack.getItem();
		MovingObjectPosition movingobjectposition = EntityUtils.getMovingObjectPositionFromPlayer(this.worldObj, player, true);
		int r;
		int g;
		int b;
		if (movingobjectposition != null && movingobjectposition.typeOfHit == MovingObjectType.BLOCK)
		{
			r = movingobjectposition.blockX;
			g = movingobjectposition.blockY;
			b = movingobjectposition.blockZ;
			if (r != this.xCoord || g != this.yCoord || b != this.zCoord)
				player.stopUsingItem();
		}
		else
			player.stopUsingItem();

		int g2;
		int b2;
		if (count % 5 == 0)
		{
			r = 1;
			if (ResearchManager.isResearchComplete(player.getCommandSenderName(), "NODETAPPER1"))
				++r;

			if (ResearchManager.isResearchComplete(player.getCommandSenderName(), "NODETAPPER2"))
				++r;

			boolean var13 = !player.isSneaking() && ResearchManager.isResearchComplete(player.getCommandSenderName(), "NODEPRESERVE") && !wand.getRod(wandstack).getTag().equals("wood") && !wand.getCap(wandstack).getTag().equals("iron");
			boolean var14 = false;
			Aspect r2 = null;
			if ((r2 = this.chooseRandomFilteredFromSource(wand.getAspectsWithRoom(wandstack), var13)) != null)
			{
				g2 = this.getAspects().getAmount(r2);
				if (r > g2)
					r = g2;

				if (var13 && r == g2)
					--r;

				if (r > 0)
				{
					b2 = wand.addVis(wandstack, r2, r, !this.worldObj.isRemote);
					if (b2 < r)
					{
						this.drainColor = r2.getColor();
						if (!this.worldObj.isRemote)
						{
							this.takeFromContainer(r2, r - b2);
							mfu = true;
						}

						var14 = true;
					}
				}
			}

			if (var14)
			{
				this.drainEntity = player;
				this.drainCollision = movingobjectposition;
				this.targetColor = new Color(this.drainColor);
			}
			else
			{
				this.drainEntity = null;
				this.drainCollision = null;
			}

			if (mfu)
			{
				this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
				this.markDirty();
			}
		}

		if (player.worldObj.isRemote)
		{
			r = this.targetColor.getRed();
			g = this.targetColor.getGreen();
			b = this.targetColor.getBlue();
			int var15 = this.color.getRed() * 4;
			g2 = this.color.getGreen() * 4;
			b2 = this.color.getBlue() * 4;
			this.color = new Color((r + var15) / 5, (g + g2) / 5, (b + b2) / 5);
		}

	}

	@Override
	public void onWandStoppedUsing(ItemStack wandstack, World world, EntityPlayer player, int count)
	{
		this.drainEntity = null;
		this.drainCollision = null;
	}

	@Override
	public boolean receiveClientEvent(int i, int j)
	{
		return super.receiveClientEvent(i, j);
	}

	@Override
	public boolean takeFromContainer(AspectList ot)
	{
		return false;
	}

	@Override
	public boolean doesContainerContainAmount(Aspect tag, int amount)
	{
		return false;
	}

	@Override
	public boolean doesContainerContain(AspectList ot)
	{
		return false;
	}

	@Override
	public int containerContains(Aspect tag)
	{
		return 0;
	}

	@Override
	public boolean doesContainerAccept(Aspect tag)
	{
		return true;
	}

	private boolean handleHungryNodeFirst(boolean change)
	{
		if (this.getNodeType() == NodeType.HUNGRY)
		{
			if (this.worldObj.isRemote)
				for (int i = 0; i < Thaumcraft.proxy.particleCount(1); ++i)
				{
					int i$ = this.xCoord + this.worldObj.rand.nextInt(16) - this.worldObj.rand.nextInt(16);
					int ent = this.yCoord + this.worldObj.rand.nextInt(16) - this.worldObj.rand.nextInt(16);
					int eo = this.zCoord + this.worldObj.rand.nextInt(16) - this.worldObj.rand.nextInt(16);
					if (ent > this.worldObj.getHeightValue(i$, eo))
						ent = this.worldObj.getHeightValue(i$, eo);

					Vec3 vec = Vec3.createVectorHelper(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D);
					Vec3 vec1 = Vec3.createVectorHelper(i$ + 0.5D, ent + 0.5D, eo + 0.5D);
					MovingObjectPosition mop = ThaumcraftApiHelper.rayTraceIgnoringSource(this.worldObj, vec, vec1, true, false, false);
					if (mop != null && this.getDistanceFrom(mop.blockX, mop.blockY, mop.blockZ) < 256.0D)
					{
						i$ = mop.blockX;
						ent = mop.blockY;
						eo = mop.blockZ;
						Block block = this.worldObj.getBlock(i$, ent, eo);
						int meta = this.worldObj.getBlockMetadata(i$, ent, eo);
						if (!block.isAir(this.worldObj, i$, ent, eo))
							Thaumcraft.proxy.hungryNodeFX(this.worldObj, i$, ent, eo, this.xCoord, this.yCoord, this.zCoord, block, meta);
					}
				}

			if (Config.hardNode)
			{
				List<Entity> entities = this.worldObj.getEntitiesWithinAABB(Entity.class, AxisAlignedBB.getBoundingBox(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1, this.yCoord + 1, this.zCoord + 1).expand(15.0D, 15.0D, 15.0D));
				if (entities != null && entities.size() > 0)
				{
					Iterator<Entity> entitiesIter = entities.iterator();

					while (entitiesIter.hasNext())
					{
						Entity entity = entitiesIter.next();
						if (!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).capabilities.disableDamage)
						{
							// TODO gamerforEA code start
							if (EventUtils.cantDamage(ModUtils.getModFake(this.worldObj), entity))
								continue;
							// TODO gamerforEA code end

							if (entity.isEntityAlive() && !entity.isEntityInvulnerable())
							{
								double distance = this.getDistanceTo(entity.posX, entity.posY, entity.posZ);
								if (distance < 2.0D)
								{
									entity.attackEntityFrom(DamageSource.outOfWorld, 1.0F);
									if (!entity.isEntityAlive() && !this.worldObj.isRemote)
									{
										ScanResult scanResult = new ScanResult((byte) 2, 0, 0, entity, "");
										AspectList aspects = ScanManager.getScanAspects(scanResult, this.worldObj);
										if (aspects != null && aspects.size() > 0)
										{
											aspects = ResearchManager.reduceToPrimals(aspects.copy());
											if (aspects != null && aspects.size() > 0)
											{
												Aspect aspect = aspects.getAspects()[this.worldObj.rand.nextInt(aspects.size())];
												if (this.getAspects().getAmount(aspect) < this.getNodeVisBase(aspect))
												{
													this.addToContainer(aspect, 1);
													change = true;
												}
												else if (this.worldObj.rand.nextInt(1 + this.getNodeVisBase(aspect) * 2) < aspects.getAmount(aspect))
												{
													this.aspectsBase.add(aspect, 1);
													change = true;
												}
											}
										}
									}
								}
							}

							double motionX = (this.xCoord + 0.5D - entity.posX) / 15.0D;
							double motionY = (this.yCoord + 0.5D - entity.posY) / 15.0D;
							double motionZ = (this.zCoord + 0.5D - entity.posZ) / 15.0D;
							double offset = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
							double d = 1.0D - offset;
							if (d > 0.0D)
							{
								d *= d;
								entity.motionX += motionX / offset * d * 0.15D;
								entity.motionY += motionY / offset * d * 0.25D;
								entity.motionZ += motionZ / offset * d * 0.15D;
							}
						}
					}
				}
			}
		}

		return change;
	}

	private boolean handleDischarge(boolean change)
	{
		if (this.worldObj.getBlock(this.xCoord, this.yCoord, this.zCoord) == ConfigBlocks.blockAiry && this.getLock() != 1)
		{
			if (this.getNodeModifier() == NodeModifier.FADING)
				return change;
			else
			{
				boolean shiny = this.getNodeType() == NodeType.HUNGRY || this.getNodeModifier() == NodeModifier.BRIGHT;
				int inc = this.getNodeModifier() == null ? 2 : shiny ? 1 : this.getNodeModifier() == NodeModifier.PALE ? 3 : 2;
				if (this.count % inc != 0)
					return change;
				else
				{
					int x = this.worldObj.rand.nextInt(5) - this.worldObj.rand.nextInt(5);
					int y = this.worldObj.rand.nextInt(5) - this.worldObj.rand.nextInt(5);
					int z = this.worldObj.rand.nextInt(5) - this.worldObj.rand.nextInt(5);
					if (this.getNodeModifier() == NodeModifier.PALE && this.worldObj.rand.nextBoolean())
						return change;
					else
					{
						if (x != 0 || y != 0 || z != 0)
						{
							TileEntity te = this.worldObj.getTileEntity(this.xCoord + x, this.yCoord + y, this.zCoord + z);
							if (te != null && te instanceof INode && this.worldObj.getBlock(this.xCoord + x, this.yCoord + y, this.zCoord + z) == ConfigBlocks.blockAiry)
							{
								if (te instanceof TileNode && ((TileNode) te).getLock() > 0)
									return change;

								INode nd = (INode) te;
								int ndavg = (nd.getAspects().visSize() + nd.getAspectsBase().visSize()) / 2;
								int thisavg = (this.getAspects().visSize() + this.getAspectsBase().visSize()) / 2;
								if (ndavg < thisavg && nd.getAspects().size() > 0)
								{
									Aspect a = nd.getAspects().getAspects()[this.worldObj.rand.nextInt(nd.getAspects().size())];
									boolean u = false;
									if (this.getAspects().getAmount(a) < this.getNodeVisBase(a) && nd.takeFromContainer(a, 1))
									{
										this.addToContainer(a, 1);
										u = true;
									}
									else if (nd.takeFromContainer(a, 1))
									{
										if (this.worldObj.rand.nextInt(1 + (int) (this.getNodeVisBase(a) / (shiny ? 1.5D : 1.0D))) == 0)
										{
											this.aspectsBase.add(a, 1);
											if (this.getNodeModifier() == NodeModifier.PALE && this.worldObj.rand.nextInt(100) == 0)
											{
												this.setNodeModifier((NodeModifier) null);
												this.regeneration = -1;
											}

											if (this.worldObj.rand.nextInt(3) == 0)
												nd.setNodeVisBase(a, (short) (nd.getNodeVisBase(a) - 1));
										}

										u = true;
									}

									if (u)
									{
										((TileNode) te).wait = ((TileNode) te).regeneration / 2;
										this.worldObj.markBlockForUpdate(this.xCoord + x, this.yCoord + y, this.zCoord + z);
										te.markDirty();
										change = true;
										PacketHandler.INSTANCE.sendToAllAround(new PacketFXBlockZap(this.xCoord + x + 0.5F, this.yCoord + y + 0.5F, this.zCoord + z + 0.5F, this.xCoord + 0.5F, this.yCoord + 0.5F, this.zCoord + 0.5F), new TargetPoint(this.worldObj.provider.dimensionId, this.xCoord, this.yCoord, this.zCoord, 32.0D));
									}
								}
							}
						}

						return change;
					}
				}
			}
		}
		else
			return change;
	}

	private boolean handleRecharge(boolean change)
	{
		if (this.regeneration < 0)
		{
			this.regeneration = 600;
			if (this.getNodeModifier() != null)
				switch (TileNode.SyntheticClass_1.$SwitchMap$thaumcraft$api$nodes$NodeModifier[this.getNodeModifier().ordinal()])
				{
					case 1:
						this.regeneration = 400;
						break;
					case 2:
						this.regeneration = 900;
						break;
					case 3:
						this.regeneration = 0;
				}

			if (this.getLock() == 1)
				this.regeneration *= 2;

			if (this.getLock() == 2)
				this.regeneration *= 20;
		}

		int len$;
		int i$;
		if (this.catchUp)
		{
			this.catchUp = false;
			long al = System.currentTimeMillis();
			len$ = this.regeneration * 75;
			i$ = len$ > 0 ? (int) ((al - this.lastActive) / len$) : 0;
			if (i$ > 0)
				for (int aspect = 0; aspect < Math.min(i$, this.aspectsBase.visSize()); ++aspect)
				{
					AspectList al1 = new AspectList();
					Aspect[] arr$1 = this.getAspects().getAspects();
					int len$1 = arr$1.length;

					for (int i$1 = 0; i$1 < len$1; ++i$1)
					{
						Aspect aspect1 = arr$1[i$1];
						if (this.getAspects().getAmount(aspect1) < this.getNodeVisBase(aspect1))
							al1.add(aspect1, 1);
					}

					if (al1.size() > 0)
						this.addToContainer(al1.getAspects()[this.worldObj.rand.nextInt(al1.size())], 1);
				}
		}

		if (this.count % 1200 == 0)
		{
			Aspect[] var12 = this.getAspects().getAspects();
			int arr$ = var12.length;

			for (len$ = 0; len$ < arr$; ++len$)
			{
				Aspect var15 = var12[len$];
				if (this.getAspects().getAmount(var15) <= 0)
				{
					this.setNodeVisBase(var15, (short) (this.getNodeVisBase(var15) - 1));
					if (this.worldObj.rand.nextInt(20) == 0 || this.getNodeVisBase(var15) <= 0)
					{
						this.getAspects().remove(var15);
						if (this.worldObj.rand.nextInt(5) == 0)
						{
							if (this.getNodeModifier() == NodeModifier.BRIGHT)
								this.setNodeModifier((NodeModifier) null);
							else if (this.getNodeModifier() == null)
								this.setNodeModifier(NodeModifier.PALE);

							if (this.getNodeModifier() == NodeModifier.PALE && this.worldObj.rand.nextInt(5) == 0)
								this.setNodeModifier(NodeModifier.FADING);
						}

						this.nodeChange();
						break;
					}

					this.nodeChange();
				}
			}

			if (this.getAspects().size() <= 0)
			{
				this.invalidate();
				if (this.getBlockType() == ConfigBlocks.blockAiry)
					this.worldObj.setBlockToAir(this.xCoord, this.yCoord, this.zCoord);
				else if (this.getBlockType() == ConfigBlocks.blockMagicalLog)
					this.worldObj.setBlockMetadataWithNotify(this.xCoord, this.yCoord, this.zCoord, this.worldObj.getBlockMetadata(this.xCoord, this.yCoord, this.zCoord) - 1, 3);
			}
		}

		if (this.wait > 0)
			--this.wait;

		if (this.regeneration > 0 && this.wait == 0 && this.count % this.regeneration == 0)
		{
			this.lastActive = System.currentTimeMillis();
			AspectList var13 = new AspectList();
			Aspect[] var14 = this.getAspects().getAspects();
			len$ = var14.length;

			for (i$ = 0; i$ < len$; ++i$)
			{
				Aspect var16 = var14[i$];
				if (this.getAspects().getAmount(var16) < this.getNodeVisBase(var16))
					var13.add(var16, 1);
			}

			if (var13.size() > 0)
			{
				this.addToContainer(var13.getAspects()[this.worldObj.rand.nextInt(var13.size())], 1);
				change = true;
			}
		}

		return change;
	}

	private boolean handleTaintNode(boolean change)
	{
		if (this.getNodeType() == NodeType.TAINTED && this.count % 50 == 0)
		{
			boolean bg2 = false;
			boolean z = false;
			boolean y = false;
			int bg3 = this.xCoord + this.worldObj.rand.nextInt(8) - this.worldObj.rand.nextInt(8);
			int z1 = this.zCoord + this.worldObj.rand.nextInt(8) - this.worldObj.rand.nextInt(8);
			BiomeGenBase bg1 = this.worldObj.getBiomeGenForCoords(bg3, z1);
			if (bg1.biomeID != ThaumcraftWorldGenerator.biomeTaint.biomeID)
				Utils.setBiomeAt(this.worldObj, bg3, z1, ThaumcraftWorldGenerator.biomeTaint);

			if (Config.hardNode && this.worldObj.rand.nextBoolean())
			{
				bg3 = this.xCoord + this.worldObj.rand.nextInt(5) - this.worldObj.rand.nextInt(5);
				z1 = this.zCoord + this.worldObj.rand.nextInt(5) - this.worldObj.rand.nextInt(5);
				int y1 = this.yCoord + this.worldObj.rand.nextInt(5) - this.worldObj.rand.nextInt(5);
				if (BlockTaintFibres.spreadFibres(this.worldObj, bg3, y1, z1))
					;
			}
		}
		else if (this.getNodeType() != NodeType.PURE && this.getNodeType() != NodeType.TAINTED && this.count % 100 == 0)
		{
			BiomeGenBase bg = this.worldObj.getBiomeGenForCoords(this.xCoord, this.zCoord);
			if (bg.biomeID == ThaumcraftWorldGenerator.biomeTaint.biomeID && this.worldObj.rand.nextInt(500) == 0)
			{
				this.setNodeType(NodeType.TAINTED);
				this.nodeChange();
			}
		}

		return change;
	}

	private boolean handleNodeStability(boolean change)
	{
		if (this.count % 100 == 0)
		{
			if (this.getNodeType() == NodeType.UNSTABLE && this.worldObj.rand.nextBoolean())
				if (this.getLock() == 0)
				{
					Aspect aspect = null;
					if ((aspect = this.takeRandomPrimalFromSource()) != null)
					{
						EntityAspectOrb orb = new EntityAspectOrb(this.worldObj, this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, aspect, 1);
						this.worldObj.spawnEntityInWorld(orb);
						change = true;
					}
				}
				else if (this.worldObj.rand.nextInt(10000 / this.getLock()) == 42)
				{
					this.setNodeType(NodeType.NORMAL);
					change = true;
				}

			if (this.getNodeModifier() == NodeModifier.FADING && this.getLock() > 0 && this.worldObj.rand.nextInt(12500 / this.getLock()) == 69)
			{
				this.setNodeModifier(NodeModifier.PALE);
				change = true;
			}
		}

		return change;
	}

	private boolean handlePureNode(boolean change)
	{
		int dimbl = ThaumcraftWorldGenerator.getDimBlacklist(this.worldObj.provider.dimensionId);
		if (this.worldObj.provider.dimensionId != -1 && this.worldObj.provider.dimensionId != 1 && dimbl != 0 && dimbl != 2 && this.getNodeType() == NodeType.PURE && this.count % 50 == 0)
		{
			int x = this.xCoord + this.worldObj.rand.nextInt(8) - this.worldObj.rand.nextInt(8);
			int z = this.zCoord + this.worldObj.rand.nextInt(8) - this.worldObj.rand.nextInt(8);
			BiomeGenBase bg = this.worldObj.getBiomeGenForCoords(x, z);
			int biobl = ThaumcraftWorldGenerator.getBiomeBlacklist(bg.biomeID);
			if (biobl != 0 && biobl != 2 && bg.biomeID != ThaumcraftWorldGenerator.biomeMagicalForest.biomeID)
				if (bg.biomeID == ThaumcraftWorldGenerator.biomeTaint.biomeID)
					Utils.setBiomeAt(this.worldObj, x, z, ThaumcraftWorldGenerator.biomeMagicalForest);
				else if (this.worldObj.getBlock(this.xCoord, this.yCoord, this.zCoord) == ConfigBlocks.blockMagicalLog)
					Utils.setBiomeAt(this.worldObj, x, z, ThaumcraftWorldGenerator.biomeMagicalForest);
		}

		return change;
	}

	private boolean handleDarkNode(boolean change)
	{
		int dimbl = ThaumcraftWorldGenerator.getDimBlacklist(this.worldObj.provider.dimensionId);
		int biobl = ThaumcraftWorldGenerator.getBiomeBlacklist(this.worldObj.getBiomeGenForCoords(this.xCoord, this.zCoord).biomeID);
		if (biobl != 0 && biobl != 2 && this.worldObj.provider.dimensionId != -1 && this.worldObj.provider.dimensionId != 1 && dimbl != 0 && dimbl != 2 && this.getNodeType() == NodeType.DARK && this.count % 50 == 0)
		{
			int x = this.xCoord + this.worldObj.rand.nextInt(12) - this.worldObj.rand.nextInt(12);
			int z = this.zCoord + this.worldObj.rand.nextInt(12) - this.worldObj.rand.nextInt(12);
			BiomeGenBase bg = this.worldObj.getBiomeGenForCoords(x, z);
			if (bg.biomeID != ThaumcraftWorldGenerator.biomeEerie.biomeID)
				Utils.setBiomeAt(this.worldObj, x, z, ThaumcraftWorldGenerator.biomeEerie);

			if (Config.hardNode && this.worldObj.rand.nextBoolean() && this.worldObj.getClosestPlayer(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D, 24.0D) != null)
			{
				EntityGiantBrainyZombie entity = new EntityGiantBrainyZombie(this.worldObj);
				if (entity != null)
				{
					int j = this.worldObj.getEntitiesWithinAABB(entity.getClass(), AxisAlignedBB.getBoundingBox(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1, this.yCoord + 1, this.zCoord + 1).expand(10.0D, 6.0D, 10.0D)).size();
					if (j <= 3)
					{
						double d0 = this.xCoord + (this.worldObj.rand.nextDouble() - this.worldObj.rand.nextDouble()) * 5.0D;
						double d3 = this.yCoord + this.worldObj.rand.nextInt(3) - 1;
						double d4 = this.zCoord + (this.worldObj.rand.nextDouble() - this.worldObj.rand.nextDouble()) * 5.0D;
						EntityGiantBrainyZombie entityliving = entity instanceof EntityLiving ? entity : null;
						entity.setLocationAndAngles(d0, d3, d4, this.worldObj.rand.nextFloat() * 360.0F, 0.0F);
						if (entityliving == null || entityliving.getCanSpawnHere())
						{
							this.worldObj.spawnEntityInWorld(entityliving);
							this.worldObj.playAuxSFX(2004, this.xCoord, this.yCoord, this.zCoord, 0);
							if (entityliving != null)
								entityliving.spawnExplosionParticle();
						}
					}
				}
			}
		}

		return change;
	}

	private boolean handleHungryNodeSecond(boolean change)
	{
		if (this.getNodeType() == NodeType.HUNGRY && this.count % 50 == 0)
		{
			int x = this.xCoord + this.worldObj.rand.nextInt(16) - this.worldObj.rand.nextInt(16);
			int y = this.yCoord + this.worldObj.rand.nextInt(16) - this.worldObj.rand.nextInt(16);
			int z = this.zCoord + this.worldObj.rand.nextInt(16) - this.worldObj.rand.nextInt(16);
			if (y > this.worldObj.getHeightValue(x, z))
				y = this.worldObj.getHeightValue(x, z);

			Vec3 v1 = Vec3.createVectorHelper(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D);
			Vec3 v2 = Vec3.createVectorHelper(x + 0.5D, y + 0.5D, z + 0.5D);
			MovingObjectPosition mop = ThaumcraftApiHelper.rayTraceIgnoringSource(this.worldObj, v1, v2, true, false, false);
			if (mop != null && this.getDistanceFrom(mop.blockX, mop.blockY, mop.blockZ) < 256.0D)
			{
				x = mop.blockX;
				y = mop.blockY;
				z = mop.blockZ;
				Block block = this.worldObj.getBlock(x, y, z);
				if (!block.isAir(this.worldObj, x, y, z))
				{
					float hardness = block.getBlockHardness(this.worldObj, x, y, z);
					// TODO gamerforEA add condition [3]
					if (hardness >= 0.0F && hardness < 5.0F && !EventUtils.cantBreak(ModUtils.getModFake(this.worldObj), x, y, z))
						this.worldObj.func_147480_a(x, y, z, true);
				}
			}
		}

		return change;
	}

	public byte getLock()
	{
		return this.nodeLock;
	}

	public void checkLock()
	{
		if ((this.count <= 1 || this.count % 50 == 0) && this.yCoord > 0 && this.getBlockType() == ConfigBlocks.blockAiry)
		{
			byte oldLock = this.nodeLock;
			this.nodeLock = 0;
			if (!this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord - 1, this.zCoord) && this.worldObj.getBlock(this.xCoord, this.yCoord - 1, this.zCoord) == ConfigBlocks.blockStoneDevice)
				if (this.worldObj.getBlockMetadata(this.xCoord, this.yCoord - 1, this.zCoord) == 9)
					this.nodeLock = 1;
				else if (this.worldObj.getBlockMetadata(this.xCoord, this.yCoord - 1, this.zCoord) == 10)
					this.nodeLock = 2;

			if (oldLock != this.nodeLock)
				this.regeneration = -1;
		}

	}

	// $FF: synthetic class
	static class SyntheticClass_1
	{
		// $FF: synthetic field
		static final int[] $SwitchMap$thaumcraft$api$nodes$NodeModifier = new int[NodeModifier.values().length];

		static
		{
			try
			{
				$SwitchMap$thaumcraft$api$nodes$NodeModifier[NodeModifier.BRIGHT.ordinal()] = 1;
			}
			catch (NoSuchFieldError var3)
			{
				;
			}

			try
			{
				$SwitchMap$thaumcraft$api$nodes$NodeModifier[NodeModifier.PALE.ordinal()] = 2;
			}
			catch (NoSuchFieldError var2)
			{
				;
			}

			try
			{
				$SwitchMap$thaumcraft$api$nodes$NodeModifier[NodeModifier.FADING.ordinal()] = 3;
			}
			catch (NoSuchFieldError var1)
			{
				;
			}

		}
	}
}
