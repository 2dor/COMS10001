"use strict";

/**
 * Constructs a new messenger to communicate with the matchmaker and the judge.
 *
 * @param url the url of the matchmaker.
 */
var GameMessenger = function () {};

GameMessenger.prototype = Object.create(Messenger.prototype);
GameMessenger.prototype.constructor = GameMessenger;

/**
 * Decodes a message and acts accordingly.
 *
 * @param message the message to react to.
 */
GameMessenger.prototype.handleMessage = function (message) {
  var decodedMessage = JSON.parse(message);
  if (debug) console.log("MESSAGE IN:" + decodedMessage.type);
  switch (decodedMessage.type) {
    case "REGISTERED":
      this.interpretRegistered(decodedMessage);
      break;
    case "READY":
      this.interpretReady(decodedMessage);
      break;
    case "PENDING_GAME":
      this.interpretPendingGame(decodedMessage);
      break;
    case "NOTIFY_TURN":
      this.interpretNotifyTurn(decodedMessage);
      break;
    case "NOTIFY":
      this.interpretNotify(decodedMessage);
      break;
    case "GAME_OVER":
      this.interpretGameOver(decodedMessage);
      break;
    case "GAMES":
      this.interpretGames(decodedMessage);
      break;
    case "CONNECTION":
      this.interpretConnection(decodedMessage);
    default:
      break;
  }
};

/**
 * Shows the view containing the game id.
 *
 * @param messageRegistered the REGISTERED message.
 */
GameMessenger.prototype.interpretRegistered = function (messageRegistered) {
	gameId = messageRegistered['game_id'];
	guiConnector.showGameIdPane(gameId);
	this.storedMessage = messageRegistered;
};

/**
 * Updates the UI with the information contianed in the READY message.
 *
 * @param messageReady the READY message.
 */
GameMessenger.prototype.interpretReady = function (messageReady) {
	var locations = messageReady['locations'];
	guiConnector.setPlayerLocations(locations);
	var tickets = messageReady['tickets'];
	guiConnector.setPlayerTickets(tickets);
	var rounds = messageReady['rounds'];
	var currentRound = messageReady['current_round'];
	guiConnector.setTicketView(rounds,currentRound);
	var colours = messageReady.colours;
	//for (var i = 0; i < colours.length; i++) {
		if (AIPlayers.length > 0 && aiMessenger && aiMessenger.isConnected()){
			aiMessenger.sendMessage(messageReady);
		}
	//}
	//why does this work?
	guiConnector.setSetUpViewVisible(false);
};

/**
 * Shows the pending game view (Where you wait until your oppenent joins).
 *
 * @param messagePendingGame the PENDING_GAME message.
 */
GameMessenger.prototype.interpretPendingGame = function (messagePendingGame) {
	var missingPlayers = messagePendingGame['opponents'];
	guiConnector.showStringInSetUpView("Waiting for: " + missingPlayers.toString());
};

/**
 * Updates the UI accordingly using the information in the NOTIFY message.
 *
 * @param messageNotify the NOTIFY message.
 */
GameMessenger.prototype.interpretNotify = function (messageNotify) {
	if (messageNotify.move_type != "MovePass") {
		var player = messageNotify.move.colour;
		var location = messageNotify.move.target;
		var ticket = messageNotify.move.ticket;
        // notify the ai (if it's connected and there is at least one AI player in the game)
        // regardless of the player who moved
		if (messageNotify.move_type == "MoveDouble") {
			guiConnector.removeTicket(player, "Double");
		} else {
			guiConnector.removeTicket(player, ticket);
			guiConnector.animatePlayer(player, location);
		}
		if (player == "Black") {
			guiConnector.updateTicketView(ticket, location);
		}
	}
    if (aiMessenger && aiMessenger.isConnected() && AIPlayers.length > 0 /*&& AIPlayers.indexOf(messageNotify.move.colour) !== -1*/) {
        aiMessenger.sendMessage(messageNotify);
    }
};

/**
 * Shows the SetUpView again and sets up for a new game.
 * If there are some AI players, it notifies the AI of the game over.
 *
 * @param messageGameOver the GAME_OVER message.
 */
GameMessenger.prototype.interpretGameOver = function (messageGameOver) {
	guiConnector.setGameOver(messageGameOver);
    if (AIPlayers.length > 0 && aiMessenger.isConnected())
        aiMessenger.sendMessage(messageGameOver);
};

/**
 * Updates the list of available games to spectate.
 *
 * @param messageGames the GAMES message.
 */
GameMessenger.prototype.interpretGames = function (messageGames) {
	guiConnector.updateGamesList(messageGames['games']);
};

/**
 * Sends a spectate message to the judge whose connection information is
 * contained within the CONNECTION message.
 *
 * @param messageConnection the CONNECTION message.
 */
GameMessenger.prototype.interpretConnection = function (messageConnection) {
  var url = "ws://" + messageConnection.host + ":" + messageConnection.port;
  this.changeConnection(url);
  // where should we get the type of the message from?
  var message = { game_id: guiConnector.getSelectedGame(),
  				  type: "SPECTATE"};
  this.sendMessage(message);
};

/**
 * Sends the notify turn message to the AI server if the current player is an AI player.
 * Updates the views accordingly.
 *
 * @param messageNotifyTurn the NOTIFY_TURN message.
 */
GameMessenger.prototype.interpretNotifyTurn = function (messageNotifyTurn) {
	var colour = messageNotifyTurn.valid_moves[0].move.colour;
	if (AIPlayers.length > 0 && aiMessenger && aiMessenger.isConnected() && AIPlayers.indexOf(colour) !== -1) {
		guiConnector.startTurn(messageNotifyTurn, true);
        aiMessenger.sendMessage(messageNotifyTurn);
	} else {
	 	guiConnector.startTurn(messageNotifyTurn, false);
	}
};

/**
 * Sends a JOIN message to the server whose connection details are contained in the REGISTERED message.
 *
 * @param message the REGISTERED message containing connection and game information.
 */
GameMessenger.prototype.sendJoin = function () {
  var self = this;
  var message = this.storedMessage;
  var callback = function () {
    var colours = message.colours;
    var aiColours = [];

    for (var i = 0; i < colours.length; i++) {
      if (AIPlayers.indexOf(colours[i]) != -1) {
        aiColours.push(colours[i]);
      } else {
        var joinMessage = {};
        joinMessage['type'] = "JOIN";
        joinMessage['colour'] =  colours[i];
        joinMessage['game_id'] = message['game_id'];
        self.sendMessage(joinMessage);
      }
    }
    if (aiColours.length > 0 && aiMessenger && aiMessenger.isConnected()) {
      message.colours = aiColours;
      if (aiMessenger) aiMessenger.sendMessage(message);
    }
  };
  var precallback = function () {
    if (self.isConnected()) {
      callback();
    }
  };
  this.changeConnection("ws://" + message.host + ":" + message.port, precallback);
};
