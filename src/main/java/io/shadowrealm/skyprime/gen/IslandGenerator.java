package io.shadowrealm.skyprime.gen;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Leaves;
import org.bukkit.material.Stairs;
import org.bukkit.material.Wood;
import org.bukkit.util.Vector;

import mortar.api.nms.ChunkTracker;
import mortar.api.nms.NMP;
import mortar.api.sched.A;
import mortar.api.sched.S;
import mortar.api.world.Direction;
import mortar.api.world.MaterialBlock;
import mortar.compute.math.Average;
import mortar.compute.math.M;
import mortar.compute.noise.SimplexOctaveGenerator;
import mortar.lang.collection.Callback;
import mortar.lang.collection.GList;
import mortar.lang.collection.GMap;
import mortar.lang.collection.GSet;

public class IslandGenerator
{

	@Getter
	@Setter
	protected int octaves;

	@Getter
	protected Location center;

	@Getter
	@Setter
	protected long seed;


	@Getter
	@Setter
	protected int radiusBlocks;

	@Getter
	@Setter
	protected double divisor;

	@Getter
	@Setter
	protected double dimension;

	@Getter
	@Setter
	protected double noise;

	@Getter
	@Setter
	protected double amplifier;

	@Getter
	@Setter
	protected double frequency;

	@Getter
	@Setter
	protected double squashTop;

	@Getter
	@Setter
	protected double squashBottom;

	protected double total;
	protected double at;
	protected double steps;
	protected double tsteps;
	protected double furthest;

	@Setter
	@Getter
	protected String status;

	protected Location spawn;
	protected ChunkTracker ct;
	protected Location chestAt;
	protected GList<Location> validSpawns;

	public IslandGenerator(Location center, long seed)
	{
		this.center = center;
		this.seed = seed;
		divisor = 100D;
		noise = 5D;
		radiusBlocks = 8;
		dimension = 0.5;
		amplifier = 0.5;
		frequency = 0.5;
		octaves = 6;
		squashTop = 1.7;
		squashBottom = 17;
		total = 0;
		at = 0;
		steps = 0;
		tsteps = 8;
		furthest = 2;
		status = "Idle";
		validSpawns = new GList<Location>();
	}

	public double getProgress()
	{
		if(at > total)
		{
			total = at;
		}

		if(total == 0)
		{
			total = 1;
		}

		return ((at / total) + (7 * (steps / tsteps))) / 8;
	}

	public IslandGenerator(Location center)
	{
		this(center, (long) (Math.random() * Long.MAX_VALUE));
	}

	public void generate(Callback<Integer> cb)
	{
		new A()
		{
			@Override
			public void run()
			{
				rset("Modeling");
				GSet<Vector> vv = warpSphere(radiusBlocks);
				rset("Dialating");
				vv = dialate(vv);
				rset("Squashing");
				vv = flatten(vv);
				rset("Rounding");
				vv = round(vv);
				rset("Realizing");
				GMap<Vector, MaterialBlock> mv = materialize(vv);
				spawn = validSpawns.isEmpty() ? center.clone() : validSpawns.pickRandom();
				ct = new ChunkTracker();
				rset("Building");
				total += mv.size() * 2;
				GSet<Location> gc = new GSet<Location>();
				boolean chested = false;

				for(Vector i : mv.k())
				{
					gc.add(center.clone().add(i));

					if(i.getX() > furthest)
					{
						furthest = i.getX();
					}

					if(i.getZ() > furthest)
					{
						furthest = i.getZ();
					}
				}

				new S()
				{
					@Override
					public void run()
					{
						GSet<Chunk> c = new GSet<Chunk>();

						for(Location i : gc)
						{
							c.add(i.getChunk());
						}

						for(Chunk i : c)
						{
							i.load();
						}

						new A()
						{
							@Override
							public void run()
							{
								for(Vector i : mv.k())
								{
									try
									{
										at++;
										ct.hit(center.clone().add(i));
										Location lxx = center.clone().add(i);
										NMP.host.setBlock(lxx, mv.get(i));

										if(!chested && mv.get(i).getMaterial().equals(Material.LOG) || mv.get(i).getMaterial().equals(Material.LOG_2))
										{
											chestAt = lxx;
										}
									}

									catch(Throwable e)
									{
										e.printStackTrace();
									}
								}

								new S()
								{
									@Override
									public void run()
									{
										try
										{
											chestAt.getBlock().setType(Material.CHEST);
											Chest c = (Chest) chestAt.getBlock().getState();
											c.getInventory().addItem(new ItemStack(Material.WATER_BUCKET));
											c.getInventory().addItem(new ItemStack(Material.WATER_BUCKET));
											c.getInventory().addItem(new ItemStack(Material.LAVA_BUCKET));

											if(M.r(0.35))
											{
												c.getInventory().addItem(new ItemStack(Material.BEETROOT_SEEDS));
											}

											else
											{
												c.getInventory().addItem(new ItemStack(Material.SEEDS));
											}

										}

										catch(Throwable e)
										{

										}

										cb.run(mv.size());
									}
								};
							}
						};
					}
				};
			}

		};
	}

	public double getTotal()
	{
		return total;
	}

	public double getAt()
	{
		return at;
	}

	public double getSteps()
	{
		return steps;
	}

	public double getTsteps()
	{
		return tsteps;
	}

	public double getFurthest()
	{
		return furthest;
	}

	public String getStatus()
	{
		return status;
	}

	public Location getChestAt()
	{
		return chestAt;
	}

	public GList<Location> getValidSpawns()
	{
		return validSpawns;
	}

	public ChunkTracker getCt()
	{
		return ct;
	}

	public Location getSpawn()
	{
		return spawn;
	}

	private void rset(String string)
	{
		at = 0;
		total = 0;
		status = string;
		steps++;
	}

	public GList<Vector> reorder(GList<Vector> k)
	{
		GList<Vector> gg = new GList<Vector>();

		while(!k.isEmpty())
		{
			Vector low = null;
			int lst = Integer.MAX_VALUE;

			for(Vector i : k)
			{
				if(i.getBlockY() < lst)
				{
					lst = i.getBlockY();
					low = i;
				}
			}

			k.remove(low);
			gg.add(low);
		}

		return gg;
	}

	public GSet<Vector> blend(GSet<Vector> in)
	{
		return in;
	}

	@SuppressWarnings("deprecation")
	public GMap<Vector, MaterialBlock> materialize(GSet<Vector> v)
	{
		GMap<Vector, MaterialBlock> mat = new GMap<Vector, MaterialBlock>();
		GMap<Vector, Integer> heightmap = getHeightmap(v);
		total += v.size() * 3;
		boolean treeyet = false;
		int shrubs = (int) (((radiusBlocks * 0.35) * Math.random()) + 1);

		Average ax = new Average(8);
		Average az = new Average(8);

		for(Vector i : v)
		{
			if(i.getX() > furthest)
			{
				furthest = i.getX();
			}

			if(i.getZ() > furthest)
			{
				furthest = i.getZ();
			}

			ax.put(i.getX());
			az.put(i.getZ());
			at++;
		}

		for(Vector i : v)
		{
			at++;

			Vector cursor = new Vector(i.getBlockX(), 0, i.getBlockZ());

			if(heightmap.get(cursor) == (int) i.getBlockY())
			{
				validSpawns.add(center.clone().add(i));

				if(validSpawns.size() > 16)
				{
					validSpawns.popRandom();
				}

				if(M.r(0.75))
				{
					mat.put(i, new MaterialBlock(Material.GRASS));
				}

				else if(M.r(0.75))
				{
					mat.put(i, new MaterialBlock(Material.DIRT, (byte) (M.r(0.45) ? 2 : 1)));
				}

				else
				{
					mat.put(i, new MaterialBlock(Material.SAND, (byte) (M.r(0.45) ? 0 : 1)));
				}

				if(M.r(0.17) && !v.contains(i.clone().add(new Vector(0, 1, 0))))
				{
					if(M.r(0.05))
					{
						mat.put(i.clone().add(new Vector(0, 1, 0)), new MaterialBlock(Material.RED_ROSE, (byte) M.rand(0, 7)));
					}

					else if(M.r(0.45))
					{
						mat.put(i.clone().add(new Vector(0, 1, 0)), new MaterialBlock(Material.LONG_GRASS, (byte) 2));
					}

					else
					{
						mat.put(i.clone().add(new Vector(0, 1, 0)), new MaterialBlock(Material.LONG_GRASS, (byte) 1));
					}
				}

				else if(!treeyet && M.r(0.06) && !v.contains(i.clone().add(new Vector(0, 1, 0))))
				{
					treeyet = true;
					Wood w = new Wood();
					TreeSpecies ttx = TreeSpecies.values()[M.rand(0, TreeSpecies.values().length - 1)];

					try
					{
						w = new Wood(Material.LOG, ttx);
					}

					catch(IllegalArgumentException e)
					{
						w = new Wood(Material.LOG_2, ttx);
					}

					int h = 8;

					GSet<Vector> vl = warpSphereTree(2);
					total += vl.size() * 3;
					vl = flatten(vl);

					for(int j = 0; j < h; j++)
					{
						mat.put(i.clone().add(new Vector(0, j, 0)), new MaterialBlock(w.getItemType(), (byte) w.getData()));
					}

					for(Vector j : vl)
					{
						at++;
						Vector jj = j.clone().add(i).add(new Vector(0, h, 0));
						if(!mat.containsKey(jj))
						{
							Leaves l = new Leaves();
							TreeSpecies tt = TreeSpecies.values()[M.rand(0, TreeSpecies.values().length - 1)];

							try
							{
								l = new Leaves(Material.LEAVES, tt);
							}

							catch(IllegalArgumentException e)
							{
								l = new Leaves(Material.LEAVES_2, tt);
							}

							mat.put(jj, new MaterialBlock(l.getItemType(), l.getData()));
						}
					}
				}

				else if(shrubs > 0 && M.r(0.015) && !v.contains(i.clone().add(new Vector(0, 1, 0))))
				{
					shrubs--;
					Wood w = new Wood();
					TreeSpecies ttx = TreeSpecies.values()[M.rand(0, TreeSpecies.values().length - 1)];

					try
					{
						w = new Wood(Material.LOG, ttx);
					}

					catch(IllegalArgumentException e)
					{
						w = new Wood(Material.LOG_2, ttx);
					}

					int h = 5;

					GSet<Vector> vl = warpSphereTree(2);
					total += vl.size() * 3;
					vl = flatten(vl);

					for(int j = 0; j < h; j++)
					{
						mat.put(i.clone().add(new Vector(0, j, 0)), new MaterialBlock(w.getItemType(), (byte) w.getData()));
					}

					for(Vector j : vl)
					{
						at++;
						Vector jj = j.clone().add(i).add(new Vector(0, h, 0));
						if(!mat.containsKey(jj))
						{
							Leaves l = new Leaves();
							TreeSpecies tt = TreeSpecies.values()[M.rand(0, TreeSpecies.values().length - 1)];

							try
							{
								l = new Leaves(Material.LEAVES, tt);
							}

							catch(IllegalArgumentException e)
							{
								l = new Leaves(Material.LEAVES_2, tt);
							}

							mat.put(jj, new MaterialBlock(l.getItemType(), l.getData()));
						}
					}
				}
			}

			else if(heightmap.get(cursor) != (int) i.getBlockY() && heightmap.get(cursor) - (int) i.getBlockY() <= 2)
			{
				mat.put(i, new MaterialBlock(Material.DIRT, (byte) (M.r(0.5) ? 1 : M.r(0.25) ? 2 : 0)));
			}

			else if(heightmap.get(cursor) > (int) i.getBlockY() && !v.contains(i.clone().add(new Vector(0, 1, 0))))
			{
				mat.put(i, new MaterialBlock(Material.GRAVEL));
			}

			else if(heightmap.get(cursor) > (int) i.getBlockY() && !v.contains(i.clone().add(new Vector(0, -1, 0))))
			{
				if(M.r(0.25))
				{
					mat.put(i, new MaterialBlock(Material.COBBLE_WALL, (byte) (M.r(0.25) ? 1 : 0)));
				}

				else
				{
					Stairs s = new Stairs(Material.COBBLESTONE_STAIRS);
					s.setInverted(true);
					s.setFacingDirection(M.r(0.5) ? M.r(0.5) ? BlockFace.NORTH : BlockFace.EAST : M.r(0.5) ? BlockFace.SOUTH : BlockFace.WEST);
					mat.put(i, new MaterialBlock(Material.COBBLESTONE_STAIRS, (byte) s.getData()));
				}
			}

			else
			{
				mat.put(i, new MaterialBlock(M.r(0.009) ? Material.COAL_ORE : M.r(0.003) ? Material.IRON_ORE : M.r(0.004) ? Material.BONE_BLOCK : M.r(0.15) ? Material.MOSSY_COBBLESTONE : M.r(0.35) ? Material.COBBLESTONE : Material.STONE));
			}

			if(i.getX() > furthest)
			{
				furthest = i.getX();
			}

			if(i.getZ() > furthest)
			{
				furthest = i.getZ();
			}
		}

		return mat;
	}

	public GMap<Vector, Integer> getHeightmap(GSet<Vector> v)
	{
		GMap<Vector, Integer> hm = new GMap<Vector, Integer>();
		total += v.size();
		for(Vector i : v)
		{
			at++;
			Vector c = new Vector(i.getBlockX(), 0, i.getBlockZ());

			if(!hm.containsKey(c))
			{
				hm.put(c, Integer.MIN_VALUE);
			}

			if((int) i.getY() > hm.get(c))
			{
				hm.put(c, i.getBlockY());
			}
		}

		return hm;
	}

	public GSet<Vector> dialate(GSet<Vector> v)
	{
		GSet<Vector> vv = new GSet<Vector>();
		total += v.size();
		for(Vector i : v)
		{
			at++;

			for(Direction j : Direction.udnews())
			{
				vv.add(i.clone().add(new Vector(j.x(), j.y(), j.z())));
			}

			vv.add(i);
		}

		return vv;
	}

	public GSet<Vector> round(GSet<Vector> v)
	{
		GSet<Vector> roll = new GSet<Vector>();
		total += v.size();
		for(Vector i : v)
		{
			at++;
			roll.add(round(i));
		}

		return roll;
	}

	public GSet<Vector> flatten(GSet<Vector> v)
	{
		rset("Squashing");
		GSet<Vector> vv = new GSet<Vector>();
		Average a = new Average(8);
		total += v.size() * 2;
		for(Vector i : v)
		{
			at++;
			a.put(i.getBlockY());
		}

		for(Vector i : v)
		{
			at++;
			if(i.getY() > a.getAverage())
			{
				double amt = (i.getY() - a.getAverage()) / squashTop;
				vv.add(i.clone().subtract(new Vector(0, amt, 0)));
			}

			else if(i.getY() < a.getAverage())
			{
				double amt = (a.getAverage() - i.getY()) / squashBottom;
				vv.add(i.clone().add(new Vector(0, amt, 0)));
			}

			else
			{
				vv.add(i);
			}
		}

		return vv;
	}

	public GSet<Vector> warpSphere(int radius)
	{
		GSet<Vector> vm = new GSet<Vector>();
		SimplexOctaveGenerator sog = new SimplexOctaveGenerator(seed, octaves);
		sog.setScale(0.01 * noise);
		GSet<Vector> v = new GSet<Vector>();
		v.addAll(generateSphere(radius));
		total += v.size();

		for(Vector i : new GList<Vector>(v))
		{
			double n = (sog.noise(i.getX(), i.getY(), i.getZ(), frequency, amplifier, true) + 1D) / 2D;

			if(n > dimension)
			{
				int x = i.getBlockX();
				int y = i.getBlockY();
				int z = i.getBlockZ();
				int nx = (int) (sog.noise(z + y, frequency, amplifier, true) * noise);
				int ny = (int) (sog.noise(x + z, frequency, amplifier, true) * noise);
				int nz = (int) (sog.noise(y + x, frequency, amplifier, true) * noise);
				Vector nv = new Vector(nx, ny, nz).clone().add(i);

				for(double j = 1; j > 0.01; j -= 1D / noise)
				{
					vm.add(nv.clone().multiply(j));
				}
			}

			at++;
		}

		return vm;
	}

	public GSet<Vector> warpSphereTree(int radius)
	{
		GSet<Vector> vm = new GSet<Vector>();
		SimplexOctaveGenerator sog = new SimplexOctaveGenerator(seed, octaves);
		sog.setScale(0.1);
		GSet<Vector> v = new GSet<Vector>();
		v.addAll(generateSphere(radius));
		total += v.size();

		for(Vector i : new GList<Vector>(v))
		{
			double n = (sog.noise(i.getX(), i.getY(), i.getZ(), frequency, amplifier, true) + 1D) / 2D;

			if(n > 0.45)
			{
				int x = i.getBlockX();
				int y = i.getBlockY();
				int z = i.getBlockZ();
				int nx = (int) (sog.noise(z + y, frequency, amplifier, true) * 2);
				int ny = (int) (sog.noise(x + z, frequency, amplifier, true) * 2);
				int nz = (int) (sog.noise(y + x, frequency, amplifier, true) * 2);
				Vector nv = new Vector(nx, ny, nz).clone().add(i);

				for(double j = 1; j > 0.01; j -= 1D / noise)
				{
					vm.add(nv.clone().multiply(j));
				}
			}

			at++;
		}

		return vm;
	}

	public GSet<Vector> warpSphereWater(int radius)
	{
		GSet<Vector> vm = new GSet<Vector>();
		SimplexOctaveGenerator sog = new SimplexOctaveGenerator(seed, octaves);
		sog.setScale(0.1);
		GSet<Vector> v = new GSet<Vector>();
		v.addAll(generateSphere(radius));
		total += v.size();

		for(Vector i : new GList<Vector>(v))
		{
			double n = (sog.noise(i.getX(), i.getY(), i.getZ(), frequency, amplifier, true) + 1D) / 2D;

			if(n > 0.15)
			{
				int x = i.getBlockX();
				int y = i.getBlockY();
				int z = i.getBlockZ();
				int nx = (int) (sog.noise(z + y, frequency, amplifier, true) * 2);
				int ny = (int) (sog.noise(x + z, frequency, amplifier, true) * 2);
				int nz = (int) (sog.noise(y + x, frequency, amplifier, true) * 2);
				Vector nv = new Vector(nx, ny, nz).clone().add(i);

				for(double j = 1; j > 0.01; j -= 1D / noise)
				{
					vm.add(nv.clone().multiply(j));
				}
			}

			at++;
		}

		return vm;
	}

	public Vector round(Vector v)
	{
		return new Vector(v.getBlockX(), v.getBlockY(), v.getBlockZ());
	}

	public GSet<Vector> generateSphere(double radius)
	{
		return generateSphere(0, 0, 0, radius);
	}

	public GSet<Vector> generateSphere(int sx, int sy, int sz, double radius)
	{
		GSet<Vector> v = new GSet<Vector>();
		Vector pos = new Vector(sx, sy, sz);
		radius += 0.5D;
		double radiusSq = radius * radius;
		int ceilRadius = (int) Math.ceil(radius);

		for(int x = 0; x <= ceilRadius; x++)
		{
			for(int y = 0; y <= ceilRadius; y++)
			{
				for(int z = 0; z <= ceilRadius; z++)
				{
					double dSq = Math.pow(x, 2) + Math.pow(y, 2) + Math.pow(z, 2);

					if(dSq > radiusSq)
					{
						continue;
					}

					v.add(pos.clone().add(new Vector(x, y, z)));
					v.add(pos.clone().add(new Vector(-x, y, z)));
					v.add(pos.clone().add(new Vector(x, -y, z)));
					v.add(pos.clone().add(new Vector(x, y, -z)));
					v.add(pos.clone().add(new Vector(-x, -y, z)));
					v.add(pos.clone().add(new Vector(x, -y, -z)));
					v.add(pos.clone().add(new Vector(-x, y, -z)));
					v.add(pos.clone().add(new Vector(-x, -y, -z)));
				}
			}
		}

		return v;
	}
}
