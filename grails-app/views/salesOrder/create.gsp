<html>
  <head>
    <meta name="layout" content="main" />
    <meta name="stylesheet" content="invoicing-transaction" />
  </head>

  <body>
    <g:applyLayout name="create"
      model="[type: 'salesOrder', instance: salesOrderInstance]"/>

    <content tag="scripts">
      <asset:javascript src="invoicing-transaction-form" />
      <asset:script>//<![CDATA[
        new SPRINGCRM.InvoicingTransaction(
          $("#sales-order-form"), {
              checkStageTransition: false,
              stageValues: {
                  payment: 803,
                  shipping: 802
              },
              type: "S"
          });
      //]]></asset:script>
    </content>
  </body>
</html>
