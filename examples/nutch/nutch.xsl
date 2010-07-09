<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:dc="http://purl.org/dc/elements/1.1/"
        xmlns:nutch="http://www.nutch.org/opensearchrss/1.0/"
        xmlns:srw_dc="info:srw/schema/1/dc-v1.1">
<xsl:output method="xml" encoding="utf-8" indent="yes" />

<xsl:template name="format_date">
   <xsl:param name="date"/>
   <xsl:value-of select="concat(substring($date, 1, 4), '-', substring($date, 5, 2), '-', substring($date, 7, 2))" />
</xsl:template>

<xsl:template match="/">
   <xsl:variable name="title"   select="/item/title/text()"/>
   <xsl:variable name="site"    select="/item/nutch:site/text()"/>
   <xsl:variable name="desc"    select="/item/description/text()"/>
   <xsl:variable name="link"    select="/item/link/text()"/>
   <xsl:variable name="wayback" select="'http://hostiwar.webarchiv.cz:8080/wayback/'"/>
   <xsl:variable name="date"    select="/item/nutch:date/text()"/>
   <xsl:variable name="format"  select="/item/nutch:type/text()"/>
   <srw_dc:dc>
     <dc:title><xsl:value-of select="$title"/></dc:title>
     <dc:publisher><xsl:value-of select="$site"/></dc:publisher>
     <dc:date>
        <xsl:call-template name="format_date">
           <xsl:with-param name="date">
              <xsl:value-of select="$date" />
           </xsl:with-param>
        </xsl:call-template>
     </dc:date>
     <dc:description><xsl:value-of select="$desc"/></dc:description>
     <dc:identifier><xsl:value-of select="$link"/></dc:identifier>
     <dc:identifier><xsl:value-of select="concat($wayback, $date, '/', $link)"/></dc:identifier>
     <dc:identifier><xsl:value-of select="concat($wayback, '*/', $link)"/></dc:identifier>
     <dc:format><xsl:value-of select="$format"/></dc:format>
   </srw_dc:dc>
</xsl:template>
</xsl:stylesheet>

