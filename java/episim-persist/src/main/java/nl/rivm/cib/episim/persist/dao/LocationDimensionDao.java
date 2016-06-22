package nl.rivm.cib.episim.persist.dao;

import javax.measure.unit.NonSI;
import javax.persistence.Entity;

import org.jscience.geography.coordinates.LatLong;

import io.coala.time.Scheduler;
import nl.rivm.cib.episim.model.Place;
import nl.rivm.cib.episim.model.TransmissionSpace;
import nl.rivm.cib.episim.model.ZipCode;

/**
 * {@link LocationDimensionDao} is a data access object for the location
 * dimension
 * 
 * @version $Id: ccb850afe9da1c0e05dabbd3374aa241dfa9e0e2 $
 * @author Rick van Krevelen
 */
@Entity
public class LocationDimensionDao
{

	private double latitude;

	private double longitude;

	private String zipCode;

	private String postCode3;

	private String postCode4;

	private String postCode6;

	private String wijkCode;

	private String buurtCode;

	private String gemeenteCode;

	private String provincieCode;

	private String landsdeelCode;

	private String ggdCode;

	public double getLatitude()
	{
		return this.latitude;
	}

	protected void setLatitude( final double latitude )
	{
		this.latitude = latitude;
	}

	public double getLongitude()
	{
		return this.longitude;
	}

	protected void setLongitude( final double longitude )
	{
		this.longitude = longitude;
	}

	public String getZipCode()
	{
		return this.zipCode;
	}

	public String getPostCode3()
	{
		return this.postCode3;
	}

	protected void setPostCode3( final String postCode3 )
	{
		this.postCode3 = postCode3;
	}

	public String getPostCode4()
	{
		return this.postCode4;
	}

	protected void setPostCode4( final String postCode4 )
	{
		this.postCode4 = postCode4;
	}

	public String getPostCode6()
	{
		return this.postCode6;
	}

	protected void setPostCode6( final String postCode6 )
	{
		this.postCode6 = postCode6;
	}

	public String getWijkCode()
	{
		return this.wijkCode;
	}

	protected void setWijkCode( final String wijkCode )
	{
		this.wijkCode = wijkCode;
	}

	public String getBuurtCode()
	{
		return this.buurtCode;
	}

	protected void setBuurtCode( final String buurtCode )
	{
		this.buurtCode = buurtCode;
	}

	public String getGemeenteCode()
	{
		return this.gemeenteCode;
	}

	protected void setGemeenteCode( final String gemeenteCode )
	{
		this.gemeenteCode = gemeenteCode;
	}

	public String getProvincieCode()
	{
		return this.provincieCode;
	}

	protected void setProvincieCode( final String provincieCode )
	{
		this.provincieCode = provincieCode;
	}

	public String getLandsdeelCode()
	{
		return this.landsdeelCode;
	}

	protected void setLandsdeelCode( final String landsdeelCode )
	{
		this.landsdeelCode = landsdeelCode;
	}

	public String getGgdCode()
	{
		return this.ggdCode;
	}

	protected void setGgdCode( final String ggdCode )
	{
		this.ggdCode = ggdCode;
	}

	public static LocationDimensionDao of( final Place location )
	{
		final LocationDimensionDao result = new LocationDimensionDao();
		result.latitude = location.getCentroid()
				.latitudeValue( NonSI.DEGREE_ANGLE );
		result.longitude = location.getCentroid()
				.longitudeValue( NonSI.DEGREE_ANGLE );
		result.postCode3 = location.getZip().toPostCode3();
		result.postCode4 = location.getZip().toPostCode4();
		result.postCode6 = location.getZip().toPostCode6();
		return result;
	}

	public Place toLocation( final Scheduler scheduler )
	{
		final TransmissionSpace space = null;
		return Place.Simple.of(
				LatLong.valueOf( this.latitude, this.longitude,
						NonSI.DEGREE_ANGLE ),
				ZipCode.valueOf( this.zipCode ), space );
	}

}
