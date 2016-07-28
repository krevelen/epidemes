// Sets the require.js configuration for your application.
require.config({

	// 3rd party script alias names (Easier to type "jquery" than
	// "libs/jquery-1.8.2.min")
	paths : {

	// Core Libraries
	// "jquery" : "lib/jquery",
	// "jquerymobile" : "lib/jquery.mobile",
	// "underscore" : "lib/lodash",
	// "backbone" : "lib/backbone",
	// "highcharts" : "lib/highcharts"
	},

	// Sets the configuration for your third party scripts that are not AMD
	// compatible
	shim : {
		underscore : {
			exports : "_"
		},
		jquery : {
			exports : "$"
		},
		backbone : {
			deps : [ "underscore", "jquery" ],
			exports : "Backbone" // attaches "Backbone" to the window object
		},
		'jquery.validate' : {
			deps : [ 'jquery' ]
		},
		jquerymobile : {
			deps : [ 'jquery', 'mobileinit' ]
		}

	}
// end Shim Configuration

});

// Includes File Dependencies
require([ "jquery", "backbone" ], function($, Backbone, Mobile) {

	$(document).on("mobileinit",
	// Set up the "mobileinit" handler before requiring jQuery Mobile's module
	function() {
		// Prevents all anchor click handling including the addition of active
		// button state and alternate link bluring.
		$.mobile.linkBindingEnabled = false;

		// Disabling this will prevent jQuery Mobile from handling hash changes
		$.mobile.hashListeningEnabled = false;
	})

	require([ "jquerymobile" ], function() {
		// Instantiates a new Backbone.js Mobile Router
		this.router = new Mobile();
	});
});

require([ 'evejs' ], function(eve) {

	/**
	 * Custom agent prototype
	 * 
	 * @param {String}
	 *            id
	 * @constructor
	 * @extend eve.Agent
	 */
	function HelloAgent(id) {
		// execute super constructor
		eve.Agent.call(this, id);
		// connect to all transports configured by the system
		this.connect(eve.system.transports.getAll());
	}

	// extend the eve.Agent prototype
	HelloAgent.prototype = Object.create(eve.Agent.prototype);
	HelloAgent.prototype.constructor = HelloAgent;

	/**
	 * Send a greeting to an agent
	 * 
	 * @param {String}
	 *            to
	 */
	HelloAgent.prototype.sayHello = function(to) {
		this.send(to, 'Hello ' + to + '!');
	};

	/**
	 * Handle incoming greetings. This overloads the default receive, so we
	 * can't use HelloAgent.on(pattern, listener) anymore
	 * 
	 * @param {String}
	 *            from Id of the sender
	 * @param {*}
	 *            message Received message, a JSON object (often a string)
	 */
	HelloAgent.prototype.receive = function(from, message) {
		document.write(from + ' said: ' + JSON.stringify(message) + '<br>');
		if (message.indexOf('Hello') === 0) {
			// reply to the greeting
			this.send(from, 'Hi ' + from + ', nice to meet you!');
		}
	};

	// create two agents
	var agent1 = new HelloAgent('agent1');
	var agent2 = new HelloAgent('agent2');

	// send a message to agent1
	agent2.send('agent1', 'Hello agent1!');
});
