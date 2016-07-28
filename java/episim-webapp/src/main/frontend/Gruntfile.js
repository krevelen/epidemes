'use strict';

module.exports = function(grunt) {
	// load all grunt tasks
	require('matchdep').filterDev('grunt-*').forEach(grunt.loadNpmTasks);

	grunt
			.initConfig({
				clean : {
					all : [ '.tmp/*'
					// , 'css/main.css'
					]
				},
				watch : {
					styles : {
						files : [ '**/*.css' ],
						tasks : [ 'compass', 'cssmin' ]
					},
					scripts : {
						files : [ '**/*.js' ],
						tasks : [ 'jshint' ],
						options : {
							spawn : false,
						},
					},
				},
				compass : {
					options : {
						sassDir : 'sass',
						cssDir : '.tmp/css',
						imagesDir : 'images',
						javascriptsDir : 'js',
						fontsDir : 'fonts',
						importPath : 'bower_components',
						relativeAssets : true
					},
					dist : {},
					dev : {
						options : {
							debugInfo : true
						}
					}
				},
				cssmin : {
					dist : {
						files : {
							'../webapp/css/style.css' : [ '.tmp/css/style.css',
									'bower_components/jquery-mobile/**/*.css' ]
						}
					}
				},
				requirejs : {
					dist : {
						options : {
							baseUrl : 'js',
							optimize : 'none',
							name : 'main',
							out : '.tmp/js/main.js',
							mainConfigFile : 'js/main.js',
							paths : {
								"jquery" : "../bower_components/jquery/jquery",
								"jquerymobile" : "../bower_components/jquery-mobile/jquery.mobile",
								"underscore" : "../bower_components/lodash/lodash",
								"backbone" : "../bower_components/backbone/backbone",
								"evejs" : "../node_modules/evejs/index",
								"highcharts" : "../bower_components/highcharts/highcharts"
							// 'templates' : '../../../../.tmp/js/templates'
							}
						}
					}
				},
				uglify : {
					dist : {
						files : {
							'../webapp/scripts/lib/evejs.js' : 'node_modules/evejs/index.js',
							'../webapp/scripts/lib/require.js' : 'bower_components/requirejs/require.js',
							'../webapp/scripts/lib/lodash.js' : 'bower_components/lodash/lodash.js',
							'../webapp/scripts/lib/backbone.js' : 'bower_components/bakcbone/backbone-min.js',
							'../webapp/scripts/lib/jquery.js' : 'bower_components/jquery/jquery.js',
							'../webapp/scripts/lib/jquerymobile.js' : 'bower_components/jquery-mobile/jquery.mobile.js',
							'../webapp/scripts/lib/highmaps.js' : 'bower_components/highcharts/highmaps.js',
							'../webapp/scripts/lib/highcharts.js' : 'bower_components/highcharts/highcharts.js',
							'../webapp/scripts/lib/highstock.js' : 'bower_components/highcharts/highstock.js',
							'../webapp/scripts/main.js' : '.tmp/js/main.js'
						}
					}
				}
			});

	grunt.registerTask('default', [ 'clean'
	// , 'jshint'
	// , 'compass:dist',
	// , 'cssmin'
	// , 'handlebars'
	, 'requirejs'
	// , 'uglify'
	//
	]);
};