import { BaseView } from "../BaseView.js";
import { GameView } from "./GameView.js";

const CREATE_NEW_GAME_STR = 'Create new game';

class PreLobbyView extends BaseView {
    

    _initialize() {
        this._log("This view is " + this.viewId);
        userID.innerHTML = this.viewId;
        this.showGamesList();
    }

    _subscribeAll() {
        this.subscribe(this.viewId, "game-created", this.handleJoinCreateGame);
    }

    showGamesList() {
        const list = this._addObjectToHTML('ul', "", gamesListContainer);
        this.model.gamesList.forEach(game => this.addListItem(game.id, list));
        this.addListItem(CREATE_NEW_GAME_STR, list);
    }

    addListItem(game, list) {
        const item = this._addObjectToHTML('li', "", list);
        item.textContent = game;
        item.addEventListener('click', () => this.handleJoinCreateGame(game));
    }

    handleJoinCreateGame(game) {
        if (game == CREATE_NEW_GAME_STR) {
            this.publish(this.model.id, "create-game", this.viewId);
        } else {
            //join game
            this._log("Joining game " + game);
            new GameView({model: this.model.gamesList[this.model.gamesList.findIndex(g => g.id === game)], parent: this});
            this.detach();
        }
    }

    _gameOver() {
    }
}

export { PreLobbyView };