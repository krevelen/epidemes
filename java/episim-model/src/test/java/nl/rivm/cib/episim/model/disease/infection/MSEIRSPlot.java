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
package nl.rivm.cib.episim.model.disease.infection;

import java.io.IOException;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.stream.IntStream;

import org.aeonbits.owner.ConfigFactory;
import org.apache.commons.math3.ode.nonstiff.DormandPrince853Integrator;
import org.apache.logging.log4j.Logger;

import io.coala.log.LogUtil;
import io.coala.math.DecimalUtil;
import io.coala.util.FileUtil;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import nl.rivm.cib.episim.model.disease.infection.MSEIRSTest.SIRConfig;

/**
 * {@link MSEIRSPlot} applies JavaFX to plot results from {@link MSEIRSTest}
 * 
 * @version $Id$
 * @author Rick van Krevelen
 */
public class MSEIRSPlot extends Application
{

	/** */
	private static final Logger LOG = LogUtil.getLogger( MSEIRSPlot.class );

	@Override
	public void start( final Stage stage )
	{
		final SIRConfig conf = ConfigFactory.create( SIRConfig.class );
		final double[] t = conf.t();
		final long[] pop = conf.population();
		final double n0 = Arrays.stream( pop ).sum();
		final String[] colors = conf.colors(), colors2 = conf.colors2();

		final Pane plot = new Pane();
		plot.setPrefSize( 400, 300 );
		plot.setMinSize( 50, 50 );

		final NumberAxis xAxis = new NumberAxis( t[0], t[1],
				(t[1] - t[0]) / 10 );
		final NumberAxis yAxis = new NumberAxis( 0, n0, n0 / 10 );
		final Pane axes = new Pane();
		axes.prefHeightProperty().bind( plot.heightProperty() );
		axes.prefWidthProperty().bind( plot.widthProperty() );

		xAxis.setSide( Side.BOTTOM );
		xAxis.setMinorTickVisible( false );
		xAxis.setPrefWidth( axes.getPrefWidth() );
		xAxis.prefWidthProperty().bind( axes.widthProperty() );
		xAxis.layoutYProperty().bind( axes.heightProperty() );

		yAxis.setSide( Side.LEFT );
		yAxis.setMinorTickVisible( false );
		yAxis.setPrefHeight( axes.getPrefHeight() );
		yAxis.prefHeightProperty().bind( axes.heightProperty() );
		yAxis.layoutXProperty()
				.bind( Bindings.subtract( 1, yAxis.widthProperty() ) );
		axes.getChildren().setAll( xAxis, yAxis );

		final Label lbl = new Label( String.format(
				"R0=%.1f, recovery=%.1ft\nSIR(0)=%s", conf.reproduction(),
				conf.recovery(), Arrays.toString( pop ) ) );
		lbl.setTextAlignment( TextAlignment.CENTER );
		lbl.setTextFill( Color.WHITE );

		final Path[] deterministic = { new Path(), new Path(), new Path() };
		IntStream.range( 0, pop.length ).forEach( i ->
		{
			final Color color = Color.valueOf( colors[i] );
			final Path path = deterministic[i];
			path.setStroke( color.deriveColor( 0, 1, 1, 0.6 ) );
			path.setStrokeWidth( 2 );
			path.setClip( new Rectangle( 0, 0, plot.getPrefWidth(),
					plot.getPrefHeight() ) );
		} );

		plot.getChildren().setAll( axes );

		// fill paths with integration estimates
		final double xl = xAxis.getLowerBound(),
				sx = plot.getPrefWidth() / (xAxis.getUpperBound() - xl),
				yh = plot.getPrefHeight(),
				sy = yh / (yAxis.getUpperBound() - yAxis.getLowerBound());
		final TreeMap<Double, Integer> iDeterministic = new TreeMap<>();

		MSEIRSTest.deterministic( conf,
				() -> new DormandPrince853Integrator( 1.0E-8, 10, 1.0E-20,
						1.0E-20 ) )
				.subscribe( yt ->
				{
					iDeterministic.put( yt.getKey(),
							deterministic[0].getElements().size() );
					final double[] y = yt.getValue();
					final double x = (yt.getKey() - xl) * sx;
					for( int i = 0; i < y.length; i++ )
					{
						final double yi = yh - y[i] * sy;
						final PathElement di = deterministic[i].getElements()
								.isEmpty() ? new MoveTo( x, yi )
										: new LineTo( x, yi );
						deterministic[i].getElements().add( di );
					}
				}, e -> LOG.error( "Problem", e ),
						() -> plot.getChildren().addAll( deterministic ) );

		final Path[] stochasticTau = { new Path(), new Path(), new Path() };
		IntStream.range( 0, pop.length ).forEach( i ->
		{
			final Color color = Color.valueOf( colors[i] );
			final Path path = stochasticTau[i];
			path.setStroke( color );
			path.setStrokeWidth( 1 );
			path.setClip( new Rectangle( 0, 0, plot.getPrefWidth(),
					plot.getPrefHeight() ) );
		} );

		final TreeMap<Double, Integer> iStochasticTau = new TreeMap<>();
		MSEIRSTest.stochasticGillespie( conf ).subscribe( yt ->
		{
			final double x = (yt.getKey() - xl) * sx;
			iStochasticTau.put( yt.getKey(),
					stochasticTau[0].getElements().size() );
			final long[] y = yt.getValue();
			for( int i = 0; i < y.length; i++ )
			{
				final double yi = yh - y[i] * sy;
				final ObservableList<PathElement> path = stochasticTau[i]
						.getElements();
				if( path.isEmpty() )
				{
					path.add( new MoveTo( x, yi ) ); // first
				} else
				{
					final PathElement last = path.get( path.size() - 1 );
					final double y_prev = last instanceof MoveTo
							? ((MoveTo) last).getY() : ((LineTo) last).getY();
					path.add( new LineTo( x, y_prev ) );
					path.add( new LineTo( x, yi ) );
				}
			}
		}, e -> LOG.error( "Problem", e ),
				() -> plot.getChildren().addAll( stochasticTau ) );

		final Path[] stochasticRes = { new Path(), new Path(), new Path() };
		IntStream.range( 0, pop.length ).forEach( i ->
		{
			final Color color = Color.valueOf( colors2[i] );
			final Path path = stochasticRes[i];
			path.setStroke( color );
			path.setStrokeWidth( 1 );
			path.setClip( new Rectangle( 0, 0, plot.getPrefWidth(),
					plot.getPrefHeight() ) );
		} );

		final TreeMap<Double, Integer> iStochasticRes = new TreeMap<>();
		MSEIRSTest.stochasticSellke( conf ).subscribe( yt ->
		{
			final double x = (yt.getKey() - xl) * sx;
			iStochasticRes.put( yt.getKey(),
					stochasticRes[0].getElements().size() );
			final long[] y = yt.getValue();
			for( int i = 0; i < y.length; i++ )
			{
				final double yi = yh - y[i] * sy;
				final ObservableList<PathElement> path = stochasticRes[i]
						.getElements();
				if( path.isEmpty() )
				{
					path.add( new MoveTo( x, yi ) ); // first
				} else
				{
					final PathElement last = path.get( path.size() - 1 );
					final double y_prev = last instanceof MoveTo
							? ((MoveTo) last).getY() : ((LineTo) last).getY();
					path.add( new LineTo( x, y_prev ) );
					path.add( new LineTo( x, yi ) );
				}
			}
		}, e -> LOG.error( "Problem", e ),
				() -> plot.getChildren().addAll( stochasticRes ) );

		// auto-scale on stage/plot resize 
		// FIXME scaling around wrong origin, use ScatterChart?
//			xAxis.widthProperty()
//					.addListener( (ChangeListener<Number>) ( observable,
//						oldValue, newValue ) ->
//					{
//						final double scale = ((Double) newValue)
//								/ plot.getPrefWidth();
//						plot.getChildren().filtered( n -> n instanceof Path )
//								.forEach( n ->
//								{
//									final Path path = (Path) n;
//									path.setScaleX( scale );
//									path.setTranslateX( (path
//											.getBoundsInParent().getWidth()
//											- path.getLayoutBounds().getWidth())
//											/ 2 );
//								} );
//					} );
//			plot.heightProperty()
//					.addListener( (ChangeListener<Number>) ( observable,
//						oldValue, newValue ) ->
//					{
//						final double scale = ((Double) newValue)
//								/ plot.getPrefHeight();
//						plot.getChildren().filtered( n -> n instanceof Path )
//								.forEach( n ->
//								{
//									final Path path = (Path) n;
//									path.setScaleY( scale );
//									path.setTranslateY(
//											(path.getBoundsInParent()
//													.getHeight() * (scale - 1))
//													/ 2 );
//								} );
//					} );

		final StackPane layout = new StackPane( lbl, plot );
		layout.setAlignment( Pos.TOP_CENTER );
		layout.setPadding( new Insets( 50 ) );
		layout.setStyle( "-fx-background-color: rgb(35, 39, 50);" );

		final Line vertiCross = new Line();
		vertiCross.setStroke( Color.SILVER );
		vertiCross.setStrokeWidth( 1 );
		vertiCross.setVisible( false );
		axes.getChildren().add( vertiCross );

		final Tooltip tip = new Tooltip( "" );
		tip.setAutoHide( false );
		tip.hide();
		axes.setOnMouseExited( ev -> tip.hide() );
		axes.setOnMouseMoved( ev ->
		{
			final Double x = (Double) xAxis.getValueForDisplay( ev.getX() );
			if( x > xAxis.getUpperBound() || x < xAxis.getLowerBound() )
			{
				tip.hide();
				vertiCross.setVisible( false );
				return;
			}
			final Double y = (Double) yAxis.getValueForDisplay( ev.getY() );
			if( y > yAxis.getUpperBound() || y < yAxis.getLowerBound() )
			{
				tip.hide();
				vertiCross.setVisible( false );
				return;
			}
			final double xs = xAxis.getDisplayPosition( x );
			vertiCross.setStartX( xs );
			vertiCross.setStartY( yAxis.getDisplayPosition( 0 ) );
			vertiCross.setEndX( xs );
			vertiCross.setEndY(
					yAxis.getDisplayPosition( yAxis.getUpperBound() ) );
			vertiCross.setVisible( true );
			final int i = (iDeterministic.firstKey() > x
					? iDeterministic.firstEntry()
					: iDeterministic.floorEntry( x )).getValue();
			final Object[] yi = Arrays.stream( deterministic )
					.mapToDouble( p -> getY( p, i ) )
					.mapToObj( yAxis::getValueForDisplay )
					.map( n -> DecimalUtil.toScale( n, 1 ) ).toArray();
			final int j = (iStochasticTau.firstKey() > x
					? iStochasticTau.firstEntry()
					: iStochasticTau.floorEntry( x )).getValue();
			final Object[] yj = Arrays.stream( stochasticTau )
					.mapToDouble( p -> getY( p, j ) )
					.mapToObj( yAxis::getValueForDisplay )
					.map( n -> DecimalUtil.toScale( n, 0 ) ).toArray();
			final int k = (iStochasticRes.firstKey() > x
					? iStochasticRes.firstEntry()
					: iStochasticRes.floorEntry( x )).getValue();
			final Object[] yk = Arrays.stream( stochasticRes )
					.mapToDouble( p -> getY( p, k ) )
					.mapToObj( yAxis::getValueForDisplay )
					.map( n -> DecimalUtil.toScale( n, 0 ) ).toArray();
			final String txt = String.format(
					"SIR(t=%.1f)\n" + "~det%s\n" + "~tau%s\n" + "~res%s", x,
					Arrays.toString( yi ), Arrays.toString( yj ),
					Arrays.toString( yk ) );

			tip.setText( txt );
			tip.show( axes, ev.getScreenX() - ev.getSceneX() + xs,
					ev.getScreenY() + 15 );
		} );

		try
		{
			stage.getIcons()
					.add( new Image( FileUtil.toInputStream( "icon.jpg" ) ) );
		} catch( final IOException e )
		{
			LOG.error( "Problem", e );
		}
		stage.setTitle( "Deterministic vs. Stochastic" );
		stage.setScene( new Scene( layout, Color.rgb( 35, 39, 50 ) ) );
//			stage.setOnHidden( ev -> tip.hide() );
		stage.show();
	}

	private double getY( final Path path, final int i )
	{
		final PathElement elem = path.getElements().get( i );
		return elem instanceof MoveTo ? ((MoveTo) elem).getY()
				: ((LineTo) elem).getY();
	}
}