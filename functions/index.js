const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

// 1) Dispara quando criam uma nova solicitação
exports.onSolicitacaoCreated = functions.firestore
    .document("solicitacoes/{solId}")
    .onCreate(async (snap) => {
      const solicit = snap.data();
      const notif = {
        titulo: "Nova Solicitação",
        mensagem: `Nova solicitação para o item ${solicit.itemId}.`,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        lida: false,
      };

      // busca todos os admins
      const adminsSnap = await admin.firestore()
          .collection("users")
          .where("isAdmin", "==", true)
          .get();

      const batch = admin.firestore().batch();
      const pushPromises = [];

      adminsSnap.forEach((doc) => {
        const adminId = doc.id;
        const adminToken = doc.get("fcmToken");

        // grava no subcollection notificacoes
        const ref = admin.firestore()
            .collection("users")
            .doc(adminId)
            .collection("notificacoes")
            .doc();
        batch.set(ref, notif);

        // dispara o push
        if (adminToken) {
          pushPromises.push(
              admin.messaging().sendToDevice(adminToken, {
                notification: {
                  title: notif.titulo,
                  body: notif.mensagem,
                },
              }),
          );
        }
      });

      // aplica todas as gravações em lote
      await batch.commit();
      // aguarda também todos os pushes
      return Promise.all(pushPromises);
    });

// 2) Dispara quando mudam o campo `status` de uma solicitação
exports.onSolicitacaoUpdated = functions.firestore
    .document("solicitacoes/{solId}")
    .onUpdate(async (change) => {
      const before = change.before.data();
      const after = change.after.data();

      // só prossegue se o status realmente mudou
      if (before.status === after.status) {
        return null;
      }

      const notif = {
        titulo: `Solicitação ${after.status}`,
        mensagem: `Seu pedido passou para: ${after.status}`,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        lida: false,
      };

      // grava na subcoleção do usuário
      const userNotifRef = admin.firestore()
          .collection("users")
          .doc(after.userId)
          .collection("notificacoes")
          .doc();
      const writePromise = userNotifRef.set(notif);

      // envia o push, se existir token
      const userDoc = await admin.firestore()
          .collection("users")
          .doc(after.userId)
          .get();
      const token = userDoc.get("fcmToken");
      let pushPromise = Promise.resolve();
      if (token) {
        pushPromise = admin.messaging().sendToDevice(token, {
          notification: {
            title: notif.titulo,
            body: notif.mensagem,
          },
        });
      }

      return Promise.all([writePromise, pushPromise]);
    });
