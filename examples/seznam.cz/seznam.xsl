<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.0"
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:dc="http://purl.org/dc/elements/1.1/"
        xmlns:nutch="http://www.nutch.org/opensearchrss/1.0/"
        xmlns:srw_dc="info:srw/schema/1/dc-v1.1">
<xsl:output method="xml" encoding="utf-8" indent="yes" />

<xsl:template match="/">
   <xsl:variable name="title"   select="/item/title/text()"/>
   <xsl:variable name="desc"    select="/item/description/text()"/>
   <xsl:variable name="link"    select="/item/link/text()"/>
   <srw_dc:dc>
     <dc:title><xsl:value-of select="$title"/></dc:title>
     <dc:description><xsl:value-of select="$desc"/></dc:description>
     <dc:identifier><xsl:value-of select="$link"/></dc:identifier>
   </srw_dc:dc>
</xsl:template>
</xsl:stylesheet>

