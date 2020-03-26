package nl.rivm.cib.epidemes.model;

import java.text.ParseException;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.coala.bind.LocalBinder;
import io.coala.name.Identified;

public interface TimedGatherer extends SocialGatherer
{
	String TYPE_KEY = "motor";

	String SITE_SCOPE_KEY = "site-scope-km";

	class Localized extends SocialGatherer.SimpleGatherer
		implements TimedGatherer
	{
		// call site broker to locate a specific site type 

		@Override
		public double maxKm()
		{
			return fromConfig( SITE_SCOPE_KEY, 100d );
		}

	}

	@Singleton
	class SimpleFactory implements SocialGatherer.Factory<TimedGatherer>
	{
		@Inject
		private LocalBinder binder;

		@Override
		public TimedGatherer create( final String name,
			final ObjectNode config )
			throws ClassNotFoundException, ParseException
		{
			final Class<? extends TimedGatherer> type = config
					.has( TYPE_KEY )
							? Class
									.forName(
											config.get( TYPE_KEY )
													.textValue() )
									.asSubclass( TimedGatherer.class )
							: TimedGatherer.Localized.class;
			return this.binder.inject( type,
					config.put( Identified.ID_JSON_PROPERTY, name ) );
		}
	}

	/**
	 * @return
	 */
	double maxKm();
}