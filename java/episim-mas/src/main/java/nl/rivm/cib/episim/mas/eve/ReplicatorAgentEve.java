package nl.rivm.cib.episim.mas.eve;

import com.almende.eve.agent.Agent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import nl.rivm.cib.episim.mas.ReplicatorAgent;

/**
 * {@link ReplicatorAgentEve}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class ReplicatorAgentEve extends Agent implements ReplicatorAgent
{

	@Override
	public String getType()
	{
		return getClass().getName() + " $Id$";
	}

	@Override
	public JsonNode resume( final JsonNode config )
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonNode status()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JsonNode pause()
	{
		new ObjectMapper( new YAMLFactory() );
		// TODO Auto-generated method stub
		return status();
	}

}
