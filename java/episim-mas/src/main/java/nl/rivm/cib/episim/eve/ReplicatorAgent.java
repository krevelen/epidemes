package nl.rivm.cib.episim.eve;

import com.almende.eve.agent.Agent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;

import nl.rivm.cib.episim.api.ReplicatorAPI;

/**
 * {@link ReplicatorAgent}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class ReplicatorAgent extends Agent implements ReplicatorAPI
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
		new ObjectMapper(new YAMLFactory());
		// TODO Auto-generated method stub
		return status();
	}

}
