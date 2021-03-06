<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:fo="http://www.w3.org/1999/XSL/Format">

  <!--===========================================

    IMPORTS

  ============================================-->

  <xsl:import href="/public/print/default/invoicing-transaction.xsl"/>


  <!--===========================================

    DEFINITIONS

  ============================================-->

  <xsl:param name="sum-label">Mahnsumme</xsl:param>


  <!--===========================================

    SELECTOR TEMPLATES

  ============================================-->

  <xsl:template match="entry[@key='transaction']">

    <!-- address field -->
    <xsl:call-template name="address-field"/>

    <!-- dunning specifications -->
    <xsl:call-template name="transaction-specification">
      <xsl:with-param name="number-label">Mahnungsnummer</xsl:with-param>
      <xsl:with-param name="additional-specifications">
        <fo:table-row>
          <fo:table-cell padding-after="{$space.paragraph}mm">
            <fo:block><xsl:text>Zu Rechnung:</xsl:text></fo:block>
          </fo:table-cell>
          <fo:table-cell padding-after="{$space.paragraph}mm"
            text-align="right">
            <fo:block>
              <xsl:value-of select="key('entries', 'invoiceFullNumber')"/>
            </fo:block>
          </fo:table-cell>
        </fo:table-row>
      </xsl:with-param>
    </xsl:call-template>

    <!-- letter caption -->
    <xsl:call-template name="header-text">
      <xsl:with-param name="transaction-type-label">
        <xsl:value-of select="level/@id - 2299"/>
        <xsl:text>. Mahnung</xsl:text>
      </xsl:with-param>
      <xsl:with-param name="additional-text">
        <xsl:text>Diese Mahnung bezieht sich auf Rechnung Nr. </xsl:text>
        <xsl:value-of select="key('entries', 'invoiceFullNumber')"/>
        <xsl:text> vom </xsl:text>
        <xsl:call-template name="format-date-long">
          <xsl:with-param name="date"
            select="key('entries', 'invoice')/docDate"/>
        </xsl:call-template>
        <xsl:text>.</xsl:text>
      </xsl:with-param>
    </xsl:call-template>

    <!-- dunning items -->
    <xsl:call-template name="items"/>

    <!-- footer text -->
    <fo:block-container line-height="{$line-height.default}"
      keep-together.within-page="always">
      <xsl:apply-templates select="footerText"/>
      <fo:block space-after="{$space.default}mm" text-align="justify">
        <xsl:text>Sollte sich diese Mahnung mit Ihrer Zahlung überschnitten
        haben, betrachten Sie dieses Schreiben bitte als
        gegenstandslos.</xsl:text>
      </fo:block>
    </fo:block-container>

    <fo:block-container line-height="{$line-height.default}"
      keep-together.within-page="always">
      <fo:block space-after="{$space.default}mm" text-align="justify">
        <xsl:text>Bitte zahlen Sie den Rechnungsbetrag in Höhe von </xsl:text>
        <xsl:value-of
          select="format-number(number(key('entries', 'invoice')/total), '#.##0,00')"/>
        <xsl:text> € und den oben angegebenen Mahnbetrag in Höhe
        von </xsl:text>
        <xsl:value-of select="format-number(number(total), '#.##0,00')"/>
        <xsl:text> € unter Angabe der o. g. Rechnungs- bzw. Mahnungsnummer bis
        zum </xsl:text>
        <xsl:call-template name="format-date-long">
          <xsl:with-param name="date" select="dueDatePayment"/>
        </xsl:call-template>
        <xsl:choose>
          <xsl:when test="(key('client', 'bankName') != '') and (key('client', 'accountNumber') != '') and (key('client', 'bankCode') != '')">
            <xsl:text> auf das folgende Konto ein:</xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>.</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
      </fo:block>
      <xsl:if test="(key('client', 'bankName') != '') and (key('client', 'accountNumber') != '') and (key('client', 'bankCode') != '')">
        <fo:block font-weight="bold">
          <xsl:value-of select="key('client', 'bankName')"/>
        </fo:block>
        <fo:block>
          <xsl:text>IBAN: </xsl:text>
          <xsl:value-of select="key('client', 'accountNumber')"/>
        </fo:block>
        <fo:block space-after="{$space.default}mm">
          <xsl:text>BIC: </xsl:text>
          <xsl:value-of select="key('client', 'bankCode')"/>
        </fo:block>
      </xsl:if>
    </fo:block-container>

    <fo:block-container line-height="{$line-height.default}"
      keep-together.within-page="always">
      <fo:block space-after="{$space.default}mm" text-align="justify">
        <xsl:text>Sollte bis zum </xsl:text>
        <xsl:call-template name="format-date-long">
          <xsl:with-param name="date" select="dueDatePayment"/>
        </xsl:call-template>
        <xsl:text> kein Zahlungseingang zu verzeichnen sein, behalten wir uns
        rechtliche Schritte vor.</xsl:text>
      </fo:block>

      <fo:block space-after="{$space.default}mm" text-align="justify">
        <xsl:text>Es gelten unsere Allgemeinen Geschäftsbedingungen. Für
        weitere Fragen stehen wir gern zur Verfügung. Sie erreichen uns unter
        den oben angegebenen Hotline-Nummern.</xsl:text>
      </fo:block>

      <xsl:call-template name="signature"/>
    </fo:block-container>
  </xsl:template>
</xsl:stylesheet>
