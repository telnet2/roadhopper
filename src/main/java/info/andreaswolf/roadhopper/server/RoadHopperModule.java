package info.andreaswolf.roadhopper.server;

import com.graphhopper.GraphHopper;
import com.graphhopper.http.DefaultModule;
import com.graphhopper.util.CmdArgs;
import info.andreaswolf.roadhopper.RoadHopper;


public class RoadHopperModule extends DefaultModule
{
	public RoadHopperModule(CmdArgs args)
	{
		super(args);
	}

	@Override
	protected GraphHopper createGraphHopper(CmdArgs args)
	{
		GraphHopper roadHopper = new RoadHopper().forServer().init(args);
		roadHopper.importOrLoad();

		return roadHopper;
	}

	@Override
	protected void configure()
	{
		super.configure();
		bind(RoadHopper.class).toInstance((RoadHopper)getGraphHopper());
	}
}