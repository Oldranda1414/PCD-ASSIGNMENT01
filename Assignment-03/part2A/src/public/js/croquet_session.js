// Croquet Tutorial 1
// Hello World 
// Croquet Corporation 
// 2021

import { FirstView } from "./FirstView.js";
import { PreLobbyModel } from "./Model/model.js";

Croquet.Session.join({
    apiKey: '1HE1txmpJCe5Cp8Pzd3Dpmq4a9gu6PqKhar4tcHtq',
    appId: 'it.unibo.studio.filippo.gurioli.microverse',
    name: "unnamed",
    password: "secret",
    model: PreLobbyModel,
    view: FirstView
});