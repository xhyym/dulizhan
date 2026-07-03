import assert from "node:assert/strict";
import {
  buildProductsPageHref,
  categoryContainsId,
  getInitialExpandedCategoryIds,
} from "./products-page.ts";

function testBuildProductsPageHref() {
  assert.equal(
    buildProductsPageHref({
      categoryId: 12,
      keyword: "sofa",
    }),
    "/products?category=12&keyword=sofa"
  );

  assert.equal(
    buildProductsPageHref({
      keyword: "table lamp",
    }),
    "/products?keyword=table+lamp"
  );

  assert.equal(
    buildProductsPageHref({}),
    "/products"
  );
}

function testCategoryContainsId() {
  const categories = [
    {
      id: 1,
      children: [
        { id: 11, children: [] },
        { id: 12, children: [{ id: 121, children: [] }] },
      ],
    },
    { id: 2, children: [] },
  ];

  assert.equal(categoryContainsId(categories[0], 121), true);
  assert.equal(categoryContainsId(categories[0], 2), false);
  assert.equal(categoryContainsId(categories[1], 2), true);
}

function testGetInitialExpandedCategoryIds() {
  const categories = [
    {
      id: 1,
      children: [
        { id: 11, children: [] },
        { id: 12, children: [{ id: 121, children: [] }] },
      ],
    },
    { id: 2, children: [] },
  ];

  assert.deepEqual(getInitialExpandedCategoryIds(categories, 121), [1]);
  assert.deepEqual(getInitialExpandedCategoryIds(categories, 2), [2]);
  assert.deepEqual(getInitialExpandedCategoryIds(categories, undefined), []);
}

function run() {
  testBuildProductsPageHref();
  testCategoryContainsId();
  testGetInitialExpandedCategoryIds();
  console.log("products-page tests passed");
}

run();
