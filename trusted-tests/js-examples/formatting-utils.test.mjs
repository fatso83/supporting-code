import utils from "./formatting-utils.mjs";
import fakeTimers from "@sinonjs/fake-timers";
import assert from "assert";
import { afterEach, describe, it } from "node:test";

describe("formatting utils", function () {
  describe("daysSince", function () {
    let clock;
    afterEach(function () {
      clock.uninstall();
    });
    it("should diff days", function () {
      clock = fakeTimers.install({ now: new Date("2022-06-28T12:00:00Z") });
      const days = utils.daysSince("2022-06-26T11:23:11Z");
      assert.strictEqual(days, "2");
    });

    it("should handle nulls by returning blank string", function () {
      const days = utils.daysSince(null);
      assert.equal(days, "");
    });
  });
});
