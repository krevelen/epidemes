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
package nl.rivm.cib.fx;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import io.coala.log.LogUtil;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * {@link HelloWorldJavaFX}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
@SuppressWarnings( { "restriction", "rawtypes", "unchecked" } )
public class HelloWorldJavaFX extends Application
{
	/** */
	private static final Logger LOG = LogUtil
			.getLogger( HelloWorldJavaFX.class );

	@FXML
	private TextField firstNameField;

	@FXML
	private TextField lastNameField;

	@FXML
	private Label messageLabel;

	@FXML
	private LineChart chart;

	/** button calls {@link #sayHello}, as per the JavaFX mark-up */
	public void sayHello()
	{

		String firstName = firstNameField.getText();
		String lastName = lastNameField.getText();

		StringBuilder builder = new StringBuilder();

		if( !StringUtils.isEmpty( firstName ) )
		{
			builder.append( firstName );
		}

		if( !StringUtils.isEmpty( lastName ) )
		{
			if( builder.length() > 0 )
			{
				builder.append( " " );
			}
			builder.append( lastName );
		}

		if( builder.length() > 0 )
		{
			String name = builder.toString();
			LOG.debug( "Saying hello to " + name );
			messageLabel.setText( "Hello " + name );
		} else
		{
			LOG.debug(
					"Neither first name nor last name was set, saying hello to anonymous person" );
			messageLabel.setText( "Hello mysterious person" );
		}
	}

	public static void main( final String[] args ) throws Exception
	{
		launch( args );
	}

	public Parent createContent()
	{
		final ObservableList<XYChart.Series<Double, Double>> lineChartData = FXCollections
				.observableArrayList(

						new LineChart.Series<>( "Series 1",
								FXCollections.observableArrayList(

										new XYChart.Data<>( 0.0, 1.0 ),

										new XYChart.Data<>( 1.2, 1.4 ),

										new XYChart.Data<>( 2.2, 1.9 ),

										new XYChart.Data<>( 2.7, 2.3 ),

										new XYChart.Data<>( 2.9, 0.5 )

								) ),

						new LineChart.Series<>( "Series 2",
								FXCollections.observableArrayList(

										new XYChart.Data<>( 0.0, 1.6 ),

										new XYChart.Data<>( 0.8, 0.4 ),

										new XYChart.Data<>( 1.4, 2.9 ),

										new XYChart.Data<>( 2.1, 1.3 ),

										new XYChart.Data<>( 2.6, 0.9 )

								) )

		);

		final NumberAxis xAxis = new NumberAxis();
		xAxis.setAutoRanging( false );
		xAxis.setTickLabelsVisible( false );
		xAxis.setTickMarkVisible( false );

		final NumberAxis yAxis = new NumberAxis();
		yAxis.setAutoRanging( true );

		this.chart = new LineChart( xAxis, yAxis, lineChartData );
		return this.chart;

	}

	@Override
	public void start( final Stage stage ) throws Exception
	{

		LOG.info( "Starting Hello JavaFX and Maven demonstration application" );

		String fxmlFile = "/fxml/hello.fxml";
		LOG.debug( "Loading FXML for main view from: {}", fxmlFile );
		FXMLLoader loader = new FXMLLoader();
		Parent rootNode = (Parent) loader
				.load( getClass().getResourceAsStream( fxmlFile ) );

		LOG.debug( "Showing JFX scene" );
		Scene scene = new Scene( rootNode, 400, 200 );
		scene.getStylesheets().add( "/styles/styles.css" );

		stage.setTitle( "Hello JavaFX and Maven" );
		stage.setScene( scene );
		stage.show();
	}
}