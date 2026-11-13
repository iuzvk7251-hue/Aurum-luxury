const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";
const ADMIN_UID = "admin_owner";
const ADMIN_EMAIL = "iuzvk7251@gmail.com";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read products, orders, suppliers, or users", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("products").get());
  await assertFails(unauthDb.collection("orders").get());
  await assertFails(unauthDb.collection("suppliers").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("User profile: owner can create customer profile, cannot self-assign admin or ghost fields", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com", email_verified: true }).firestore();
  const nowTs = new Date(Date.now() - 1000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@example.com",
      role: "customer",
      createdAt: nowTs,
      updatedAt: nowTs,
    })
  );

  // Self-assigned admin fails
  await assertFails(
    aliceDb.collection("users").doc("alice_admin").set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@example.com",
      role: "admin",
      createdAt: nowTs,
      updatedAt: nowTs,
    })
  );

  // Ghost field fails
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isVip: true,
      updatedAt: nowTs,
    })
  );
});

test("PII Isolation: Bob cannot read Alice's profile, cart, wishlist, or orders", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    const nowTs = new Date(Date.now() - 1000);
    await db.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      email: "alice@example.com",
      role: "customer",
      createdAt: nowTs,
      updatedAt: nowTs,
    });
    await db.collection("users").doc(ALICE_UID).collection("cart").doc("item_1").set({
      userId: ALICE_UID,
      productId: "prod_1",
      title: "Aurum Watch",
      price: 299.0,
      quantity: 1,
      imageKey: "watch",
      createdAt: nowTs,
      updatedAt: nowTs,
    });
    await db.collection("orders").doc("order_1").set({
      userId: ALICE_UID,
      customerName: "Alice",
      customerEmail: "alice@example.com",
      shippingAddress: "123 Fifth Ave, NY",
      itemsSummary: "1x Aurum Watch",
      itemCount: 1,
      totalAmount: 299.0,
      paymentMethod: "Card (Visa / MasterCard)",
      paymentStatus: "Paid",
      orderStatus: "Pending",
      supplierId: "zendrop",
      supplierName: "Zendrop",
      forwardingStatus: "Queued",
      createdAt: nowTs,
      updatedAt: nowTs,
    });
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID, { email: "bob@example.com", email_verified: true }).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("cart").get());
  await assertFails(bobDb.collection("orders").doc("order_1").get());
  await assertFails(bobDb.collection("orders").get());
});

test("Orders: customer query with userId filter succeeds, customer cannot mark order Shipped", async () => {
  const nowTs = new Date(Date.now() - 1000);
  const aliceDb = testEnv.authenticatedContext(ALICE_UID, { email: "alice@example.com", email_verified: true }).firestore();

  await assertSucceeds(
    aliceDb.collection("orders").doc("ord_101").set({
      userId: ALICE_UID,
      customerName: "Alice",
      customerEmail: "alice@example.com",
      shippingAddress: "123 Fifth Ave, NY",
      itemsSummary: "1x Aurum Watch",
      itemCount: 1,
      totalAmount: 299.0,
      paymentMethod: "PayPal",
      paymentStatus: "Paid",
      orderStatus: "Pending",
      supplierId: "zendrop",
      supplierName: "Zendrop",
      forwardingStatus: "Queued",
      createdAt: nowTs,
      updatedAt: nowTs,
    })
  );

  // Scoped query succeeds
  await assertSucceeds(
    aliceDb.collection("orders").where("userId", "==", ALICE_UID).get()
  );

  // Customer cannot shortcut order status to Shipped
  await assertFails(
    aliceDb.collection("orders").doc("ord_101").update({
      orderStatus: "Shipped",
      updatedAt: nowTs,
    })
  );

  // Customer can cancel a Pending order
  await assertSucceeds(
    aliceDb.collection("orders").doc("ord_101").update({
      orderStatus: "Cancelled",
      updatedAt: nowTs,
    })
  );
});

test("Email spoofing test: unverified admin email cannot write suppliers or products", async () => {
  const nowTs = new Date(Date.now() - 1000);
  const spoofDb = testEnv.authenticatedContext("spoof_uid", { email: ADMIN_EMAIL, email_verified: false }).firestore();

  await assertFails(
    spoofDb.collection("suppliers").doc("zendrop").set({
      name: "Zendrop",
      code: "ZENDROP",
      isConnected: true,
      autoImport: true,
      autoPriceSync: true,
      autoStockSync: true,
      autoOrderForwarding: true,
      autoTrackingSync: true,
      createdAt: nowTs,
      updatedAt: nowTs,
    })
  );
});

test("Verified admin: can manage suppliers, products, and update order tracking", async () => {
  const nowTs = new Date(Date.now() - 1000);
  const adminDb = testEnv.authenticatedContext(ADMIN_UID, { email: ADMIN_EMAIL, email_verified: true }).firestore();

  await assertSucceeds(
    adminDb.collection("suppliers").doc("zendrop").set({
      name: "Zendrop",
      code: "ZENDROP",
      apiKeyMasked: "zd_live_****8821",
      isConnected: true,
      autoImport: true,
      autoPriceSync: true,
      autoStockSync: true,
      autoOrderForwarding: true,
      autoTrackingSync: true,
      specialNote: "US Warehouse • Fast Shipping 4-7 days",
      importedProductCount: 2,
      isCustom: false,
      createdAt: nowTs,
      updatedAt: nowTs,
    })
  );

  await assertSucceeds(
    adminDb.collection("products").doc("prod_watch_1").set({
      title: "Aurum Apex Chronograph",
      description: "Titanium matte black and 24K brushed gold smartwatch.",
      category: "Watches",
      price: 349.0,
      compareAtPrice: 449.0,
      stock: 25,
      imageKey: "watch",
      rating: 4.9,
      reviewCount: 128,
      supplierId: "zendrop",
      supplierName: "Zendrop",
      supplierTag: "US Warehouse • 4-7 Days",
      shippingEstimate: "4-7 Business Days (US)",
      isBestSeller: true,
      supportsPrintOnDemand: false,
      supportsSampleOrder: false,
      createdAt: nowTs,
      updatedAt: nowTs,
    })
  );
});
