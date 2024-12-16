import fakeTimers from "@sinonjs/fake-timers";
import assert from "assert";
import {afterEach, describe, it} from "node:test";
import {promisify} from "node:util";
import debug from 'debug'

const log = debug('animation:test')

class Player {
    x = 0;
}

/* Can only move in one direction - side scroller */
function movePlayer(player, xDistance) {
    const MAX_DISTANCE_PER_TICK = 100;

    return promisify(setTimeout)(16).then(async () => {
        // set DEBUG=animation:* to see the player move in steps
        log(`moving player ${xDistance} steps. Current X position is ${player.x}`)

        if (xDistance < MAX_DISTANCE_PER_TICK) {
            player.x += xDistance;
            return;
        }
        player.x += MAX_DISTANCE_PER_TICK;

        await movePlayer(player, xDistance - MAX_DISTANCE_PER_TICK);
    });
}

describe("moving a player", function () {
    let clock;
    afterEach(() => {
        clock.uninstall();
    });

    it("should should be possible to await a move", async function () {
        // Arrange
        clock = fakeTimers.install();
        const player = new Player();

        // Act
        const movePromise = movePlayer(player, 300);

        await clock.runAllAsync();
        await movePromise;
        assert.equal(player.x, 300);
    });

    it("should move in chunks of 100 steps per frame", async () => {
        // Arrange
        clock = fakeTimers.install();
        const player = new Player();

        // Act
        const movePromise = movePlayer(player, 300);

        // Assert
        assert.equal(player.x, 0);
        await clock.tickAsync(16);
        assert.equal(player.x, 100);

        await clock.tickAsync(16);
        assert.equal(player.x, 200);

        await clock.tickAsync(16);
        assert.equal(player.x, 300);

        await clock.runAllAsync();
        await movePromise;
    });
});
