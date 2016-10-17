/* $Id$
 * 
 * Part of ZonMW project no. 50-53000-98-156
 * 
 * @license
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Copyright (c) 2016 RIVM National Institute for Health and Environment 
 */
package nl.rivm.cib.episim.mas;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Date;
import java.util.List;

import javax.measure.quantity.Duration;
import javax.measure.unit.Unit;

import org.aeonbits.owner.Converter;
import org.joda.time.DateTime;

import com.eaio.uuid.UUID;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

import io.coala.config.ConfigUtil;
import io.coala.config.GlobalConfig;
import io.coala.json.JsonUtil;
import io.coala.time.Timing;

/**
 * {@link ReplicatorAgent}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 *
 */
public interface ReplicatorAgent
{

	String TIME_UNIT_KEY = "time_unit";

	String OFFSET_KEY = "offset";

	String UNTIL_KEY = "until";

	String TOPICS_KEY = "topics";

	String STEP_RATIO_KEY = "step_ratio";

	String VIRTUAL_MILLIS_KEY = "virtual_ms";

	String ACTUAL_MILLIS_KEY = "actual_ms";

	String TIME_TOPIC = "time";

	String PACE_TOPIC = "pace";

	/**
	 * {@link StepRatio}
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	interface ReplicationConfig extends GlobalConfig
	{

		@Key( TIME_UNIT_KEY )
		@DefaultValue( "day" )
		@ConverterClass( DurationUnitParser.class )
		Unit<Duration> timeUnit();

		/** the start {@link Date} of this scenario replication */
		@Key( OFFSET_KEY )
		@DefaultValue( "2015-01-01T00:00:00" )
		@ConverterClass( DateTimeParser.class )
		DateTime offset();

		/** the end {@link Date} of this scenario replication */
		@Key( UNTIL_KEY )
		@DefaultValue( "2030-01-01T00:00:00" )
		@ConverterClass( DateTimeParser.class )
		DateTime until();

		/** the kind of topic {@link String}s being published by the agent */
		@Key( TOPICS_KEY )
		@DefaultValue( TIME_TOPIC + ConfigUtil.CONFIG_VALUE_SEP + PACE_TOPIC )
		List<String> topics();

		/**
		 * {@link DurationUnitParser} helper class for parsing units of duration
		 * 
		 * @version $Id$
		 * @author Rick van Krevelen
		 */
		class DurationUnitParser implements Converter<Unit<Duration>>
		{
			@Override
			public Unit<Duration> convert( final Method method,
				final String input )
			{
				return Unit.valueOf( input ).asType( Duration.class );
			}
		}

		class DateTimeParser implements Converter<DateTime>
		{
			@Override
			public DateTime convert( final Method method, final String input )
			{
				return DateTime.parse( input );
			}
		}
	}

	/**
	 * {@link StepRatio} is a POJO for
	 * 
	 * @version $Id$
	 * @author Rick van Krevelen
	 */
	class StepRatio implements Serializable
	{
		/** the serialVersionUID */
		private static final long serialVersionUID = 1L;

		/** the current (virtual) date/time */
//		@JsonProperty( TIME_KEY )
//		public Date time;

		/** the (virtual) milliseconds between delays; =<0: paused */
		@JsonProperty( VIRTUAL_MILLIS_KEY )
		public BigDecimal virtualMS;

		/** the minimum (actual) milliseconds step delay; =<0: maximum speed */
		@JsonProperty( ACTUAL_MILLIS_KEY )
		public BigDecimal actualMS;

		/**
		 * @param virtualStepMS the (virtual) milliseconds between delays; =<0:
		 *            paused
		 * @param actualStepMS the minimum (actual) milliseconds step delay;
		 *            =<0: maximum speed
		 * @return the {@link StepRatio} POJO
		 */
		public static StepRatio of( final BigDecimal virtualStepMS,
			final BigDecimal actualStepMS )
		{
			final StepRatio result = new StepRatio();
			result.virtualMS = virtualStepMS;
			result.actualMS = actualStepMS;
			return result;
		}

		@Override
		public boolean equals( final Object rhs )
		{
			if( rhs == this ) return true;
			if( rhs == null || !StepRatio.class.isInstance( rhs ) )
				return false;
			final StepRatio that = (StepRatio) rhs;
			return (this.virtualMS == null ? that.virtualMS == null
					: this.virtualMS.equals( that.virtualMS ))
					&& (this.actualMS == null ? that.actualMS == null
							: this.actualMS.equals( that.actualMS ));
		}

		@Override
		public String toString()
		{
			return JsonUtil.stringify( this );
		}

		public JsonNode toJSON()
		{
			return JsonUtil.toTree( this );
		}

		public static StepRatio of( final JsonNode tree )
		{
			return JsonUtil.valueOf( tree, StepRatio.class );
		}
	}

	/**
	 * change (pause/resume) the simulation pace
	 * 
	 * @param pace the new {@link StepRatio}
	 */
	void myPace( StepRatio pace );

	/**
	 * @param listener the subscriber {@link URI} to notify
	 * @param topic a topic {@link String} to subscribe listener to
	 * @param timing a ISO/cron/iCal timing pattern {@link String}, or
	 *            {@code null} for any
	 * @return the subscription {@link UUID}
	 */
	UUID subscribe( URI listener, String topic, Timing timing );

	/**
	 * @param subscription the subscription {@link UUID} to cancel
	 */
	boolean unsubscribe( UUID subscription );

}
