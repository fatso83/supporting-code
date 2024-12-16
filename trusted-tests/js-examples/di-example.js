// service.js
export function makeService(repository) {
  return {
    findPatients() {
      return repository.findAll();
    },
  };
}

/// Client code
import {appContext, constructApp} from "./context";
class App {
    constructor(service) {
        this.service = service;
    }
  start() {
      console.log(service.findPatients())
  }
}

constructApp(appContext).start();

/// context.js
import realRepository from "./repository";
import fakeRepository from "./repository-fake";

export const appContext = {
    repository: realRepository
}

export const testContext = {
    repository: fakeRepository
}

export function constructApp(ctx) {
    return new App(makeService(ctx.repository));
}

// Test code constructs the app similarly, but 
// with different dependencies

import {testContext} from "./context";

it("should fetch stuff from the repo", () => {
    const app = constructApp(testContext);
    const service = app.service;
    const repo = testContext.repository

    repo.save({id:1})
    repo.save({id:2})
    assert.deepEquals(service.findAll(), [{id:1}, {id: 2}]);
})

repo.
