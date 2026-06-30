import assert from "node:assert/strict";
import {
  DEFAULT_TRANSLATE_LANGUAGE,
  normalizeTranslateLanguage,
  resolveTranslateRuntimeEnabled,
  shouldBootTranslateOnLoad,
} from "./translate-runtime.ts";

function testResolveTranslateRuntimeEnabled() {
  assert.equal(
    resolveTranslateRuntimeEnabled({
      hostname: "localhost",
      nodeEnv: "development",
      enableInDev: false,
    }),
    false
  );

  assert.equal(
    resolveTranslateRuntimeEnabled({
      hostname: "localhost",
      nodeEnv: "development",
      enableInDev: true,
    }),
    true
  );

  assert.equal(
    resolveTranslateRuntimeEnabled({
      hostname: "osenfurniture.com",
      nodeEnv: "production",
      enableInDev: false,
    }),
    true
  );
}

function testNormalizeTranslateLanguage() {
  assert.equal(normalizeTranslateLanguage(undefined), DEFAULT_TRANSLATE_LANGUAGE);
  assert.equal(normalizeTranslateLanguage(""), DEFAULT_TRANSLATE_LANGUAGE);
  assert.equal(normalizeTranslateLanguage(" french "), "french");
}

function testShouldBootTranslateOnLoad() {
  assert.equal(shouldBootTranslateOnLoad("english"), false);
  assert.equal(shouldBootTranslateOnLoad("french"), true);
  assert.equal(shouldBootTranslateOnLoad(undefined), false);
}

function run() {
  testResolveTranslateRuntimeEnabled();
  testNormalizeTranslateLanguage();
  testShouldBootTranslateOnLoad();
  console.log("translate-runtime tests passed");
}

run();
