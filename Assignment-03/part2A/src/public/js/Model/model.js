import { BaseModel } from "../BaseModel.js";
import { GameModel } from "./GameModel.js";

class PreLobbyModel extends BaseModel {

    users = [];
    gamesList = [];

    _initialize() {
        this._continuousLog();
    }

    _continuousLog() {
        console.log(this.gamesList);
        this.future(1000)._continuousLog();
    }

    _subscribeAll() {
        this.subscribe(this.sessionId, "view-join", this.viewJoin);
        this.subscribe(this.sessionId, "view-exit", this.viewDrop);
        this.subscribe(this.id, "create-game", this.createGame);
    }

    /**
     * Handle a new connected view.
     * @param {any} viewId the id of the new view connected.
     */
    viewJoin(viewId) {
        this.users.push(viewId);
    }

    /**
     * Handle the view left event.
     * @param {any} viewId the id of the outgoing view.
     */
    viewDrop(viewId) {
        this.users.splice(this.users.indexOf(viewId), 1);
    }

    createGame(creator) {
        const game = GameModel.create({ parent: this });
        this.gamesList.push(game);
        this.publish(this.id, "game-created", { game: game.id, creator: creator });
    }
}

PreLobbyModel.register("PreLobbyModel");

export { PreLobbyModel };