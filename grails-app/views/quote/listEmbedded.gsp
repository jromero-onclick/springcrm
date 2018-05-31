<html>
  <head>
    <meta name="layout" content="null"/>
  </head>

  <body>
    <g:applyLayout name="listEmbedded"
      model="[list: quoteList, total: quoteCount]">
      <table class="table data-table">
        <thead>
          <tr>
            <g:sortableColumn property="number"
              title="${message(code: 'invoicingTransaction.number.label')}"/>
            <g:sortableColumn property="subject"
              title="${message(code: 'invoicingTransaction.subject.label')}"/>
            <g:sortableColumn property="stage"
              title="${message(code: 'quote.stage.label.short')}"/>
            <g:sortableColumn property="docDate"
              title="${message(code: 'quote.docDate.label.short')}"/>
            <g:sortableColumn property="shippingDate"
              title="${message(code: 'quote.shippingDate.label')}"/>
            <g:sortableColumn property="total"
              title="${message(code: 'quote.total.label.short')}"/>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <g:each var="quote" in="${quoteList}" status="i">
            <tr>
              <td class="col-type-id quote-number">
                <g:link controller="quote" action="show" id="${quote.id}">
                  <g:fullNumber bean="${quote}"/>
                </g:link>
              </td>
              <td class="col-type-string quote-subject">
                <g:link controller="quote" action="show" id="${quote.id}">
                  <g:nl2br value="${quote.subject}"/>
                </g:link>
              </td>
              <td class="col-type-status quote-stage">
                <g:fieldValue bean="${quote}" field="stage"/>
              </td>
              <td class="col-type-date quote-doc-date">
                <g:formatDate date="${quote?.docDate}"
                  formatName="default.format.date"/>
              </td>
              <td class="col-type-date quote-shipping-date">
                <g:formatDate date="${quote?.shippingDate}"
                  formatName="default.format.date"/>
              </td>
              <td class="col-type-currency quote-total">
                <g:formatCurrency number="${quote?.total}" displayZero="true"
                  external="true"/>
              </td>
              <td class="col-actions">
                <g:button controller="quote" action="edit" id="${quote.id}"
                  color="success" size="xs" icon="pencil-square-o"
                  message="default.button.edit.label"/>
              </td>
            </tr>
          </g:each>
        </tbody>
      </table>
    </g:applyLayout>
  </body>
</html>
