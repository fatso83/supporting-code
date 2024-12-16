import repository from "./repository";

function makeService() {
  return {
    findPatients() {
      return repository.findAll();
    },
  };
}

/// Client code

class App {
  start() {
      service = makeService();
      console.log(service.findPatients())
  }
}

new App().start()

// Test code? How do I create a Repository 
// that is different from the app. How can I use that in the tests?
