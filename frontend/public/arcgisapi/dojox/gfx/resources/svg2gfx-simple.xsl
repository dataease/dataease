<?xml version="1.0" encoding="UTF-8"?>
<!-- Super simple XSLT to convert Nils.svg and Lars.svg to our format -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:fo="http://www.w3.org/1999/XSL/Format">
	<xsl:output method="text" version="1.0" encoding="UTF-8"/>
	<xsl:template name="fill">
		<xsl:param name="node"/>
		<xsl:if test="count($node/@fill) &gt; 0">
			<xsl:text>fill: "</xsl:text>
			<xsl:value-of select="$node/@fill"/>
			<xsl:text>",</xsl:text>
		</xsl:if>
	</xsl:template>
	<xsl:template name="stroke">
		<xsl:param name="node"/>
		<xsl:text>stroke: {</xsl:text>
		<xsl:if test="count($node/@stroke) &gt; 0">
			<xsl:text>color: "</xsl:text>
			<xsl:value-of select="$node/@stroke"/>
			<xsl:text>",</xsl:text>
		</xsl:if>
		<xsl:if test="count($node/@stroke-width) &gt; 0">
			<xsl:text>width: "</xsl:text>
			<xsl:value-of select="$node/@stroke-width"/>
			<xsl:text>",</xsl:text>
		</xsl:if>
		<xsl:if test="count($node/@stroke-linecap) &gt; 0">
			<xsl:text>cap: "</xsl:text>
			<xsl:value-of select="$node/@stroke-linecap"/>
			<xsl:text>",</xsl:text>
		</xsl:if>
		<xsl:if test="count($node/@stroke-linejoin) &gt; 0">
			<xsl:text>join: "</xsl:text>
			<xsl:value-of select="$node/@stroke-linejoin"/>
			<xsl:text>",</xsl:text>
		</xsl:if>
		<xsl:text>},</xsl:text>
	</xsl:template>
	<xsl:template match="g">
		<xsl:text>{</xsl:text>
		<xsl:if test="count(@id) &gt; 0">
			<xsl:text>name: "</xsl:text>
			<xsl:value-of select="@id"/>
			<xsl:text>",</xsl:text>
		</xsl:if>
		<xsl:text>children: [</xsl:text>
		<xsl:apply-templates select="g|path"/>
		<xsl:text>]},</xsl:text>
	</xsl:template>
	<xsl:template match="path">
		<xsl:text>{</xsl:text>
		<xsl:if test="count(@id) &gt; 0">
			<xsl:text>name: "</xsl:text>
			<xsl:value-of select="@id"/>
			<xsl:text>",</xsl:text>
		</xsl:if>
		<xsl:text>shape: {type: "path", path: "</xsl:text>
		<xsl:value-of select="@d"/>
		<xsl:text>"},</xsl:text>
		<xsl:call-template name="fill">
			<xsl:with-param name="node" select="."/>
		</xsl:call-template>
		<xsl:call-template name="stroke">
			<xsl:with-param name="node" select="."/>
		</xsl:call-template>
		<xsl:text>},</xsl:text>
	</xsl:template>
	<xsl:template match="svg">
		<xsl:text>[</xsl:text>
		<xsl:apply-templates select="g|path"/>
		<xsl:text>]</xsl:text>
	</xsl:template>
</xsl:stylesheet>
