<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE xsl:stylesheet [
<!ENTITY SupportedElements "svg:a|svg:circle|svg:ellipse|svg:g|svg:image|svg:line|svg:path|svg:polygon|svg:polyline|svg:rect|svg:text|svg:textPath|svg:use">
]>
<!-- This is a complete rewrite of the original svg2gfx.xslt used for testing. -->
<!--
This version supports polygons, polylines, circles, ellipses, rectangles,
lines, images, text, patterns, linear gradients, radial gradients, transforms
(although gradient transforms are limited), and more in addition to the
paths, strokes, groups, and constant fills supported by the original.  It
even handles little niceties like the SVG use element.  All that being said,
It does not even come close to supporting all of the features found in SVG,
but should hopefully be a fairly useful subset.

Caveats: Completely ignores many SVG features (such as named views, filters,
object bounding box in gradient transforms, etc.).  Now requires properly
formed SVG (that is, SVG using the appropriate SVG namespace) which most
editors create by default these days anyhow (the old version required that
namespaces be stripped off).  Can't convert to GFX constructs that cannot
be reconstructed from JSON (such as textpath or using vector fonts).
Requires EXSLT for many transforms. Handles nested styles in a simple way
that is usually right but sometimes wrong.

Questions / comments / bug reports can be sent to Feneric (on Twitter, IRC,
GMail, etc.) or Eric (Saugus.net, ShellTown, etc.)
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:svg="http://www.w3.org/2000/svg"
	xmlns:xlink="http://www.w3.org/1999/xlink"
	xmlns:math="http://exslt.org/math"
	xmlns:exsl="http://exslt.org/common"
	xmlns:saxon="http://icl.com/saxon"
	xmlns:xalan="http://xml.apache.org/Xalan"
	extension-element-prefixes="math exsl saxon xalan">
	<xsl:output method="text" version="1.0" encoding="UTF-8"/>
	<xsl:strip-space elements="*"/>

	<!-- We currently need this constant for some transformation calculations. -->
	<!-- GFX enhancements could obviate it in the future. -->
	<xsl:variable name="degressInRadian" select="57.295779513082322"/>
	
	<!-- The following templates process little bits of things that can often occur in multiple contexts -->
	
	<xsl:template name="kill-extra-spaces">
		<xsl:param name="string"/>
		<!-- Some don't feel that SVG is verbose enough and thus add extra spaces, which when -->
		<!-- untreated can look exactly like delimiters in point sets. -->
		<xsl:choose>
			<!-- Hopefully most cases won't have the extra spaces -->
			<xsl:when test="not(contains($string,', '))">
				<xsl:value-of select="$string"/>
			</xsl:when>
			<xsl:otherwise>
				<!-- We split at comma / space pairs and recursively chop spaces -->
				<xsl:call-template name="kill-extra-spaces">
					<xsl:with-param name="string" select="substring-before($string,', ')"/>
				</xsl:call-template>
				<xsl:text>,</xsl:text>
				<xsl:call-template name="kill-extra-spaces">
					<xsl:with-param name="string" select="substring-after($string,', ')"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="arg-processor">
		<xsl:param name="values"/>
		<xsl:param name="labels"/>
		<!-- Recursively chew through the arguments in a traditional CAR / CDR pattern -->
		<xsl:variable name="valuesCdr" select="substring-after($values,',')"/>
		<!-- We're going "backwards" here to take advantage of tail recursion -->
		<xsl:choose>
			<xsl:when test="not($valuesCdr)">
				<!-- handle the final argument -->
				<xsl:value-of select="$labels"/>
				<xsl:text>:</xsl:text>
				<xsl:value-of select="$values"/>
				<!-- This last trailing comma is needed in the (odd) case of multiple transforms -->
				<xsl:text>,</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<!-- handle the current argument -->
				<xsl:value-of select="substring-before($labels,',')"/>
				<xsl:text>:</xsl:text>
				<xsl:value-of select="substring-before($values,',')"/>
				<xsl:text>,</xsl:text>
				<xsl:call-template name="arg-processor">
					<xsl:with-param name="values" select="$valuesCdr"/>
					<xsl:with-param name="labels" select="substring-after($labels,',')"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="background-processor">
		<xsl:param name="background"/>
		<xsl:choose>
			<xsl:when test="starts-with($background,'url')">
				<!-- Check if we have a URL (for a gradient or pattern) -->
				<xsl:variable name="arguments" select="translate(normalize-space(substring-before(substring-after($background,'('),')')),' ',',')"/>
				<xsl:call-template name="url-processor">
					<xsl:with-param name="url" select="$arguments"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<!-- We probably have a solid color. -->
				<xsl:call-template name="color-processor">
					<xsl:with-param name="color" select="$background"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="color-processor">
		<xsl:param name="color"/>
		<xsl:choose>
			<xsl:when test="starts-with($color,'rgb')">
				<!-- Check if we have an RGB triple -->
				<xsl:variable name="arguments" select="normalize-space(substring-before(substring-after($color,'('),')'))"/>
				<xsl:call-template name="rgb-triple-processor">
					<xsl:with-param name="triple" select="$arguments"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="$color='none'">
				<!-- Check if we have a literal 'none' -->
				<!-- Literal nones seem to actually map to black in practice -->
				<xsl:text>"#000000",</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<!-- This color could either be by name or value.  Either way, we -->
				<!-- have to ensure that there are no bogus semi-colons. -->
				<xsl:text>"</xsl:text>
				<xsl:value-of select="normalize-space(translate($color,';',' '))"/>
				<xsl:text>",</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="point-processor">
		<xsl:param name="points"/>
		<!-- Recursively process points in a traditional CAR / CDR pattern -->
		<xsl:variable name="pointsCdr" select="normalize-space(substring-after($points,' '))"/>
		<!-- We're going "backwards" here to take advantage of tail recursion -->
		<xsl:choose>
			<xsl:when test="not($pointsCdr)">
				<!-- handle the final argument -->
				<xsl:text>{x:</xsl:text>
				<xsl:value-of select="substring-before($points,',')"/>
				<xsl:text>,y:</xsl:text>
				<xsl:value-of select="substring-after($points,',')"/>
				<xsl:text>},</xsl:text>
			</xsl:when>
			<xsl:otherwise>
				<!-- handle the current argument -->
				<xsl:variable name="pointsCar" select="substring-before($points,' ')"/>
				<xsl:text>{x:</xsl:text>
				<xsl:value-of select="substring-before($pointsCar,',')"/>
				<xsl:text>,y:</xsl:text>
				<xsl:value-of select="substring-after($pointsCar,',')"/>
				<xsl:text>},</xsl:text>
				<xsl:call-template name="point-processor">
					<xsl:with-param name="points" select="$pointsCdr"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="rgb-triple-processor">
		<xsl:param name="triple"/>
		<!-- Note that as SVG triples cannot contain alpha values, we hardcode it to be fully opaque -->
		<!-- This could theoretically be better handled by watching for fill-opacity -->
		<xsl:variable name="red" select="substring-before($triple,',')"/>
		<xsl:variable name="green" select="substring-before(substring-after($triple,concat($red,',')),',')"/>
		<xsl:variable name="blue" select="substring-after($triple,concat($red,',',$green,','))"/>
		<xsl:text>{"r":</xsl:text>
		<xsl:value-of select="normalize-space($red)"/>
		<xsl:text>,"g":</xsl:text>
		<xsl:value-of select="normalize-space($green)"/>
		<xsl:text>,"b":</xsl:text>
		<xsl:value-of select="normalize-space($blue)"/>
		<xsl:text>,"a":1},</xsl:text>
	</xsl:template>
	
	<xsl:template name="styles-processor">
		<xsl:param name="styles"/>
		<!-- Recursively chew through the styles in a traditional CAR / CDR pattern -->
		<xsl:variable name="stylesCdr" select="substring-after($styles,';')"/>
		<!-- We're going "backwards" here to take advantage of tail recursion -->
		<xsl:choose>
			<xsl:when test="not($stylesCdr)">
				<!-- handle the final style -->
				<xsl:attribute name="{normalize-space(substring-before($styles,':'))}">
					<xsl:value-of select="normalize-space(substring-after($styles,':'))"/>
				</xsl:attribute>
			</xsl:when>
			<xsl:otherwise>
				<!-- handle the current style -->
				<xsl:variable name="stylesCar" select="substring-before($styles,';')"/>
				<xsl:attribute name="{normalize-space(substring-before($stylesCar,':'))}">
					<xsl:value-of select="normalize-space(substring-after($stylesCar,':'))"/>
				</xsl:attribute>
				<xsl:call-template name="styles-processor">
					<xsl:with-param name="styles" select="$stylesCdr"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template name="transform-processor">
		<xsl:param name="transforms"/>
		<!-- Recursively chew through the transforms in a traditional CAR / CDR pattern -->
		<xsl:variable name="transformsCdr" select="normalize-space(substring-after($transforms,')'))"/>
		<xsl:variable name="arguments" select="translate(normalize-space(substring-before(substring-after($transforms,'('),')')),' ',',')"/>
		<xsl:choose>
			<!-- We only handle simple (i.e. nonoverlapping) chained transforms. -->
			<!-- This covers most real-world cases, and exceptions are generally -->
			<!-- hand-generated and can likewise be hand fixed. -->
			<xsl:when test="starts-with($transforms,'matrix')">
				<xsl:call-template name="arg-processor">
					<xsl:with-param name="values" select="$arguments"/>
					<xsl:with-param name="labels" select="string('xx,yx,xy,yy,dx,dy')"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="starts-with($transforms,'translate')">
				<!-- If only one argument is provided, it's assumed for both -->
				<xsl:choose>
					<xsl:when test="contains($arguments,',')">
						<xsl:call-template name="arg-processor">
							<xsl:with-param name="values" select="$arguments"/>
							<xsl:with-param name="labels" select="string('dx,dy')"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="arg-processor">
							<xsl:with-param name="values" select="concat($arguments,',',$arguments)"/>
							<xsl:with-param name="labels" select="string('dx,dy')"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="starts-with($transforms,'scale')">
				<!-- If only one argument is provided, it's assumed for both -->
				<xsl:choose>
					<xsl:when test="contains($arguments,',')">
						<xsl:call-template name="arg-processor">
							<xsl:with-param name="values" select="$arguments"/>
							<xsl:with-param name="labels" select="string('xx,yy')"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="arg-processor">
							<xsl:with-param name="values" select="concat($arguments,',',$arguments)"/>
							<xsl:with-param name="labels" select="string('xx,yy')"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="starts-with($transforms,'rotate')">
				<!-- Kluge alert - we're redoing a function GFX already provides here because -->
				<!-- GFX doesn't yet expose it to JSON input. It requires XSLT extensions, too. -->
				<!-- If you don't have the extensions, comment the following out (bye bye rotate). -->
				<xsl:choose>
					<xsl:when test="function-available('math:sin') and function-available('math:cos')">
						<xsl:variable name="sinOfAngle" select="math:sin($arguments div $degressInRadian)"/>
						<xsl:variable name="cosOfAngle" select="math:cos($arguments div $degressInRadian)"/>
						<xsl:variable name="subarguments" select="concat($cosOfAngle,',',-$sinOfAngle,',',$sinOfAngle,',',$cosOfAngle)"/>
						<xsl:call-template name="arg-processor">
							<xsl:with-param name="values" select="$subarguments"/>
							<xsl:with-param name="labels" select="string('xx,yx,xy,yy')"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:message>
							<xsl:text>exslt:sin and exslt:cos must be supported for a rotation.</xsl:text>
						</xsl:message>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="starts-with($transforms,'skewX')">
				<!-- Kluge alert - we're redoing a function GFX already provides here because -->
				<!-- GFX doesn't yet expose it to JSON input. It requires XSLT extensions, too. -->
				<!-- If you don't have the extensions, comment the following out (bye bye skewX). -->
				<xsl:choose>
					<xsl:when test="function-available('math:tan')">
						<xsl:variable name="tanOfAngle" select="math:tan($arguments div $degressInRadian)"/>
						<xsl:call-template name="arg-processor">
							<xsl:with-param name="values" select="$tanOfAngle"/>
							<xsl:with-param name="labels" select="string('xy')"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:message>
							<xsl:text>exslt:tan must be supported for a skewX.</xsl:text>
						</xsl:message>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="starts-with($transforms,'skewY')">
				<!-- Kluge alert - we're redoing a function GFX already provides here because -->
				<!-- GFX doesn't yet expose it to JSON input. It requires XSLT extensions, too. -->
				<!-- If you don't have the extensions, comment the following out (bye bye skewY). -->
				<xsl:choose>
					<xsl:when test="function-available('math:tan')">
						<xsl:variable name="tanOfAngle" select="math:tan($arguments div $degressInRadian)"/>
						<xsl:call-template name="arg-processor">
							<xsl:with-param name="values" select="$tanOfAngle"/>
							<xsl:with-param name="labels" select="string('yx')"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:message>
							<xsl:text>exslt:tan must be supported for a skewY.</xsl:text>
						</xsl:message>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
		</xsl:choose>
		<xsl:if test="$transformsCdr">
			<!-- handle the other transforms -->
			<xsl:call-template name="transform-processor">
				<xsl:with-param name="transforms" select="$transformsCdr"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>

	<xsl:template name="url-processor">
		<xsl:param name="url"/>
		<xsl:param name="groupAttrs" select="''"/>
		<!-- We can only handle local references; that's probably all we should get anyway -->
		<xsl:if test="starts-with($url,'#')">
			<xsl:apply-templates select="id(substring-after($url,'#'))">
				<xsl:with-param name="groupAttrs" select="$groupAttrs"/>
			</xsl:apply-templates>
		</xsl:if>
	</xsl:template>

	<!-- The following templates help with gradient transforms -->

	<!-- We're temporarily supporting a few SVG features that GFX does not currently support. -->
	<!-- The biggest of these is gradient transforms; when GFX natively supports it all the -->
	<!-- kluges made to support it here (including all the following code) should be removed. -->
	
	<xsl:template name="gradient-transform-helper">
		<!-- This nasty little routine helps gradient adjuster and can be -->
		<!-- removed when GFX gets gradientTransform support. -->
		<xsl:param name="cxa"/>
		<xsl:param name="cya"/>
		<xsl:param name="x1a"/>
		<xsl:param name="y1a"/>
		<xsl:param name="x2a"/>
		<xsl:param name="y2a"/>
		<xsl:param name="xx"/>
		<xsl:param name="xy"/>
		<xsl:param name="yx"/>
		<xsl:param name="yy"/>
		<xsl:param name="dx"/>
		<xsl:param name="dy"/>
		<xsl:choose>
			<xsl:when test="local-name()='radialGradient'">
				<xsl:variable name="cx" select="$xx*$cxa+$xy*$cya+$dx"/>
				<xsl:text>cx:</xsl:text>
				<xsl:value-of select="$cx"/>
				<xsl:text>,</xsl:text>
				<xsl:variable name="cy" select="$yx*$cxa+$yy*$cya+$dy"/>
				<xsl:text>cy:</xsl:text>
				<xsl:value-of select="$cy"/>
				<xsl:text>,</xsl:text>
				<!-- The results for r here are going to just be approximate -->
				<xsl:variable name="r" select="($cx+$cy) div 2"/>
				<xsl:text>r:</xsl:text>
				<xsl:value-of select="$r"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:variable name="x1" select="$xx*$x1a+$xy*$y1a+$dx"/>
				<xsl:text>x1:</xsl:text>
				<xsl:value-of select="$x1"/>
				<xsl:text>,</xsl:text>
				<xsl:variable name="y1" select="$yx*$x1a+$yy*$y1a+$dy"/>
				<xsl:text>y1:</xsl:text>
				<xsl:value-of select="$y1"/>
				<xsl:text>,</xsl:text>
				<xsl:variable name="x2" select="$xx*$x2a+$xy*$y2a+$dx"/>
				<xsl:text>x2:</xsl:text>
				<xsl:value-of select="$x2"/>
				<xsl:text>,</xsl:text>
				<xsl:variable name="y2" select="$yx*$x2a+$yy*$y2a+$dy"/>
				<xsl:text>y2:</xsl:text>
				<xsl:value-of select="$y2"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="gradient-adjuster">
		<xsl:param name="node"/>
		<!-- This code is awful and only meant to serve until GFX gets gradientTransform support. -->
		<!-- Once GFX does gradientTransforms, the following should be destroyed and forgotten. -->
		<!-- While this support is better than nothing, it cannot 100% reproduce the effects -->
		<!-- that true gradientTransform support in GFX could provide. -->
		<xsl:choose>
			<xsl:when test="starts-with($node/@gradientTransform,'matrix')">
				<xsl:variable name="args" select="normalize-space(substring-before(substring-after($node/@gradientTransform,'matrix('),')'))"/>
				<xsl:variable name="xx" select="substring-before($args,' ')"/>
				<xsl:variable name="yx" select="substring-before(substring-after($args,' '),' ')"/>
				<xsl:variable name="xy" select="substring-before(substring-after($args,concat($xx,' ',$yx,' ')),' ')"/>
				<xsl:variable name="yy" select="substring-before(substring-after($args,concat($xx,' ',$yx,' ',$xy,' ')),' ')"/>
				<xsl:variable name="dx" select="substring-before(substring-after($args,concat($xx,' ',$yx,' ',$xy,' ',$yy,' ')),' ')"/>
				<xsl:variable name="dy" select="substring-after($args,concat($xx,' ',$yx,' ',$xy,' ',$yy,' ',$dx,' '))"/>
				<xsl:call-template name="gradient-transform-helper">
					<xsl:with-param name="cxa" select="$node/@cx"/>
					<xsl:with-param name="cya" select="$node/@cy"/>
					<xsl:with-param name="x1a" select="$node/@x1"/>
					<xsl:with-param name="y1a" select="$node/@y1"/>
					<xsl:with-param name="x2a" select="$node/@x2"/>
					<xsl:with-param name="y2a" select="$node/@y2"/>
					<xsl:with-param name="xx" select="$xx"/>
					<xsl:with-param name="yx" select="$yx"/>
					<xsl:with-param name="xy" select="$xy"/>
					<xsl:with-param name="yy" select="$yy"/>
					<xsl:with-param name="dx" select="$dx"/>
					<xsl:with-param name="dy" select="$dy"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="starts-with($node/@gradientTransform,'translate')">
				<xsl:variable name="args" select="normalize-space(substring-before(substring-after($node/@gradientTransform,'translate('),')'))"/>
				<!-- If only one argument is provided, it's assumed for both -->
				<xsl:choose>
					<xsl:when test="contains($args,',')">
						<xsl:call-template name="gradient-transform-helper">
							<xsl:with-param name="cxa" select="$node/@cx"/>
							<xsl:with-param name="cya" select="$node/@cy"/>
							<xsl:with-param name="x1a" select="$node/@x1"/>
							<xsl:with-param name="y1a" select="$node/@y1"/>
							<xsl:with-param name="x2a" select="$node/@x2"/>
							<xsl:with-param name="y2a" select="$node/@y2"/>
							<xsl:with-param name="xx" select="1"/>
							<xsl:with-param name="yx" select="0"/>
							<xsl:with-param name="xy" select="1"/>
							<xsl:with-param name="yy" select="0"/>
							<xsl:with-param name="dx" select="substring-before($args,' ')"/>
							<xsl:with-param name="dy" select="substring-after($args,' ')"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="gradient-transform-helper">
							<xsl:with-param name="cxa" select="$node/@cx"/>
							<xsl:with-param name="cya" select="$node/@cy"/>
							<xsl:with-param name="x1a" select="$node/@x1"/>
							<xsl:with-param name="y1a" select="$node/@y1"/>
							<xsl:with-param name="x2a" select="$node/@x2"/>
							<xsl:with-param name="y2a" select="$node/@y2"/>
							<xsl:with-param name="xx" select="1"/>
							<xsl:with-param name="yx" select="0"/>
							<xsl:with-param name="xy" select="1"/>
							<xsl:with-param name="yy" select="0"/>
							<xsl:with-param name="dx" select="$args"/>
							<xsl:with-param name="dy" select="$args"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:when test="starts-with($node/@gradientTransform,'scale')">
				<xsl:variable name="args" select="normalize-space(substring-before(substring-after($node/@gradientTransform,'scale('),')'))"/>
				<!-- If only one argument is provided, it's assumed for both -->
				<xsl:choose>
					<xsl:when test="contains($args,',')">
						<xsl:call-template name="gradient-transform-helper">
							<xsl:with-param name="cxa" select="$node/@cx"/>
							<xsl:with-param name="cya" select="$node/@cy"/>
							<xsl:with-param name="x1a" select="$node/@x1"/>
							<xsl:with-param name="y1a" select="$node/@y1"/>
							<xsl:with-param name="x2a" select="$node/@x2"/>
							<xsl:with-param name="y2a" select="$node/@y2"/>
							<xsl:with-param name="xx" select="substring-before($args,' ')"/>
							<xsl:with-param name="yx" select="0"/>
							<xsl:with-param name="xy" select="substring-after($args,' ')"/>
							<xsl:with-param name="yy" select="0"/>
							<xsl:with-param name="dx" select="0"/>
							<xsl:with-param name="dy" select="0"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="gradient-transform-helper">
							<xsl:with-param name="cxa" select="$node/@cx"/>
							<xsl:with-param name="cya" select="$node/@cy"/>
							<xsl:with-param name="x1a" select="$node/@x1"/>
							<xsl:with-param name="y1a" select="$node/@y1"/>
							<xsl:with-param name="x2a" select="$node/@x2"/>
							<xsl:with-param name="y2a" select="$node/@y2"/>
							<xsl:with-param name="xx" select="$args"/>
							<xsl:with-param name="yx" select="0"/>
							<xsl:with-param name="xy" select="$args"/>
							<xsl:with-param name="yy" select="0"/>
							<xsl:with-param name="dx" select="0"/>
							<xsl:with-param name="dy" select="0"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>	<!-- Otherwise it's got to be a rotation -->
				<xsl:variable name="args" select="normalize-space(substring-before(substring-after($node/@gradientTransform,'rotate('),')'))"/>
				<xsl:choose>
					<xsl:when test="function-available('math:sin') and function-available('math:cos')">
						<xsl:variable name="sinOfAngle" select="math:sin($args div $degressInRadian)"/>
						<xsl:variable name="cosOfAngle" select="math:cos($args div $degressInRadian)"/>
						<xsl:call-template name="gradient-transform-helper">
							<xsl:with-param name="cxa" select="$node/@cx"/>
							<xsl:with-param name="cya" select="$node/@cy"/>
							<xsl:with-param name="x1a" select="$node/@x1"/>
							<xsl:with-param name="y1a" select="$node/@y1"/>
							<xsl:with-param name="x2a" select="$node/@x2"/>
							<xsl:with-param name="y2a" select="$node/@y2"/>
							<xsl:with-param name="xx" select="$cosOfAngle"/>
							<xsl:with-param name="yx" select="-$sinOfAngle"/>
							<xsl:with-param name="xy" select="$sinOfAngle"/>
							<xsl:with-param name="yy" select="$cosOfAngle"/>
							<xsl:with-param name="dy" select="0"/>
							<xsl:with-param name="dy" select="0"/>
						</xsl:call-template>
					</xsl:when>
					<xsl:otherwise>
						<xsl:message>
							<xsl:text>exslt:sin and exslt:cos must be supported for a gradient rotation.</xsl:text>
						</xsl:message>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text>,</xsl:text>
	</xsl:template>
	
	<!-- The following templates handle related batches of attributes -->
	
	<xsl:template name="font">
		<xsl:param name="node"/>
		<!-- Only include if we have at least some font properties defined -->
		<xsl:if test="$node/@font-style or $node/@font-variant or $node/@font-weight or $node/@font-size or $node/@font-family">
			<xsl:text>font:{ type:"font",</xsl:text>
			<xsl:if test="$node/@font-style">
				<xsl:text>style:"</xsl:text>
				<xsl:value-of select="$node/@font-style"/>
				<xsl:text>",</xsl:text>
			</xsl:if>
			<xsl:if test="$node/@font-variant">
				<xsl:text>variant:"</xsl:text>
				<xsl:value-of select="$node/@font-variant"/>
				<xsl:text>",</xsl:text>
			</xsl:if>
			<xsl:if test="$node/@font-weight">
				<xsl:text>weight:"</xsl:text>
				<xsl:value-of select="$node/@font-weight"/>
				<xsl:text>",</xsl:text>
			</xsl:if>
			<xsl:if test="$node/@font-size">
				<xsl:text>size:"</xsl:text>
				<xsl:value-of select="$node/@font-size"/>
				<xsl:text>",</xsl:text>
			</xsl:if>
			<xsl:if test="$node/@font-family">
				<xsl:text>family:"</xsl:text>
				<xsl:value-of select="$node/@font-family"/>
				<xsl:text>",</xsl:text>
			</xsl:if>
			<xsl:text>},</xsl:text>
		</xsl:if>
	</xsl:template>

	<xsl:template name="stroke">
		<xsl:param name="node"/>
		<!-- Only include if we have at least some stroke properties defined -->
		<xsl:if test="$node/@stroke or $node/@stroke-width or $node/@stroke-linecap or $node/@stroke-linejoin">
			<xsl:text>stroke:{</xsl:text>
			<!-- We don't currently handle stroke-dasharray or stroke-dashoffset -->
			<!-- Note that while we'll pass stroke background info, GFX won't yet use it. -->
			<xsl:if test="$node/@stroke">
				<xsl:text>color:</xsl:text>
				<xsl:call-template name="background-processor">
					<xsl:with-param name="background" select="$node/@stroke"/>
				</xsl:call-template>
			</xsl:if>
			<xsl:if test="$node/@stroke-width">
				<xsl:text>width:"</xsl:text>
				<xsl:value-of select="$node/@stroke-width"/>
				<xsl:text>",</xsl:text>
			</xsl:if>
			<xsl:if test="$node/@stroke-linecap">
				<xsl:text>cap:"</xsl:text>
				<xsl:value-of select="$node/@stroke-linecap"/>
				<xsl:text>",</xsl:text>
			</xsl:if>
			<xsl:if test="$node/@stroke-linejoin">
				<xsl:text>join:"</xsl:text>
				<xsl:value-of select="$node/@stroke-linejoin"/>
				<xsl:text>",</xsl:text>
			</xsl:if>
			<xsl:choose>
				<!-- This is really cheesy but better than nothing. -->
				<!-- We probably ought to match a few specific cases when we can. %FIX% -->
				<xsl:when test="$node/@stroke-dasharray">
					<xsl:text>style:"Dash",</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>style:"Solid",</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:text>},</xsl:text>
		</xsl:if>
	</xsl:template>

	<xsl:template name="common-attributes">
		<xsl:param name="node"/>
		<!-- Pretty much every shape has to handle this same batch of attributes. -->
		<xsl:apply-templates select="$node/@style"/>
		<!-- Note that we make no effort to guard against overlapping styles. -->
		<xsl:apply-templates select="$node/@fill"/>
		<xsl:call-template name="stroke">
			<xsl:with-param name="node" select="$node"/>
		</xsl:call-template>
		<xsl:apply-templates select="$node/@transform"/>
		<!-- Fonts are actually illegal in most shapes, but including them here doesn't -->
		<!-- really slow things down much and does clean up code a bit for the shapes -->
		<!-- that do allow them. -->
		<xsl:call-template name="font">
			<xsl:with-param name="node" select="$node"/>
		</xsl:call-template>
		<!-- Ditto for stop-colors. -->
		<xsl:apply-templates select="$node/@stop-color"/>
	</xsl:template>

	<!-- SVG Attribute Handling -->
	
	<xsl:template match="@id">
		<xsl:text>name:"</xsl:text>
		<xsl:apply-templates/>
		<xsl:text>",</xsl:text>
	</xsl:template>
	
	<xsl:template match="@x|@y|@x1|@x2|@y1|@y2|@cx|@cy|@r|@rx|@ry|@fx|@fy|@width|@height|@offset">
		<!-- Generic attribute followed by comma -->
		<xsl:value-of select="local-name()"/>
		<xsl:text>:</xsl:text>
		<xsl:value-of select="."/>
		<xsl:text>,</xsl:text>
	</xsl:template>
	
	<xsl:template match="@d">
		<!-- Used only by path objects; often has tons of extra whitespace -->
		<xsl:text>path:"</xsl:text>
		<xsl:value-of select="normalize-space(.)"/>
		<xsl:text>",</xsl:text>
	</xsl:template>
	
	<xsl:template match="@fill">
		<!-- Used by most shapes and can have a URL, a solid color, or "none" -->
		<xsl:if test=". != 'none'">
			<xsl:text>fill:</xsl:text>
			<xsl:call-template name="background-processor">
				<xsl:with-param name="background" select="."/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>

	<xsl:template match="@stop-color">
		<xsl:call-template name="color-processor">
			<xsl:with-param name="color" select="."/>
		</xsl:call-template>
	</xsl:template>

	<xsl:template match="@style">
		<!-- A style property is really a bunch of other properties crammed together. -->
		<!-- We therefore make a dummy element and process it as normal. -->
		<xsl:variable name="dummy">
			<dummy>
				<xsl:call-template name="styles-processor">
					<xsl:with-param name="styles" select="."/>
				</xsl:call-template>
			</dummy>
		</xsl:variable>
		<xsl:choose>
			<!-- Using a dummy element requires node-set capability. Straight XSLT 1.0 -->
			<!-- lacks this, but pretty much every XSLT processor offers it as an extension. -->
			<xsl:when test="function-available('exsl:node-set')">
				<xsl:call-template name="common-attributes">
					<xsl:with-param name="node" select="exsl:node-set($dummy)/dummy"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="function-available('saxon:node-set')">
				<xsl:call-template name="common-attributes">
					<xsl:with-param name="node" select="saxon:node-set($dummy)"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:when test="function-available('xalan:nodeSet')">
				<xsl:call-template name="common-attributes">
					<xsl:with-param name="node" select="xalan:nodeSet($dummy)"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:message>
					<xsl:text>exslt:node-set is required for processing the style attribute.</xsl:text>
				</xsl:message>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>

	<xsl:template match="@transform|@gradientTransform">
		<!-- Several transform types are supported -->
		<xsl:text>transform:{</xsl:text>
		<xsl:call-template name="transform-processor">
			<xsl:with-param name="transforms" select="."/>
		</xsl:call-template>
		<xsl:text>}</xsl:text>		
		<xsl:if test="not(position()=last())"> 
			<xsl:text >,</xsl:text> 
		</xsl:if>
	</xsl:template>

	<!-- SVG Element Handling -->
	
	<xsl:template match="svg:a">
		<xsl:param name="groupAttrs" select="''"/>
		<!-- Anchors are actually meaningless to us, but their contents should usually be processed. -->
		<xsl:variable name="newGroupAttrs">
			<xsl:value-of select="$groupAttrs"/>
			<xsl:apply-templates select="@style"/>
			<!-- Note that we make no effort to guard against overlapping styles; we just order -->
			<!-- them to be consistent.  This naive approach will usually, but not always, work. -->
			<xsl:apply-templates select="@fill"/>
			<xsl:call-template name="stroke">
				<xsl:with-param name="node" select="."/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:apply-templates select="&SupportedElements;">
			<xsl:with-param name="groupAttrs" select="$newGroupAttrs"/>
		</xsl:apply-templates>
	</xsl:template>

	<xsl:template match="svg:circle">
		<xsl:param name="groupAttrs" select="''"/>
		<xsl:text>{</xsl:text>
		<xsl:apply-templates select="@id"/>
		<xsl:text>shape:{type:"circle",</xsl:text>
		<xsl:apply-templates select="@cx|@cy|@r"/>
		<xsl:text>},</xsl:text>
		<xsl:value-of select="$groupAttrs"/>
		<xsl:call-template name="common-attributes">
			<xsl:with-param name="node" select="."/>
		</xsl:call-template>
		<xsl:text>}</xsl:text>
		<xsl:if test="not(position()=last())"> 
			<xsl:text >,</xsl:text> 
		</xsl:if>
	</xsl:template>

	<xsl:template match="svg:ellipse">
		<xsl:param name="groupAttrs" select="''"/>
		<xsl:text>{</xsl:text>
		<xsl:apply-templates select="@id"/>
		<xsl:text>shape:{type:"ellipse",</xsl:text>
		<xsl:apply-templates select="@cx|@cy|@rx|@ry"/>
		<xsl:text>}</xsl:text>
		<xsl:value-of select="$groupAttrs"/>
		<xsl:call-template name="common-attributes">
			<xsl:with-param name="node" select="."/>
		</xsl:call-template>
		<xsl:text>}</xsl:text>
		<xsl:if test="not(position()=last())"> 
			<xsl:text >,</xsl:text> 
		</xsl:if>
	</xsl:template>

	<xsl:template match="svg:g">
		<xsl:param name="groupAttrs" select="''"/>
		<!-- The basic grouping type can contain shapes, other groups, and have a transform -->
		<xsl:text>{</xsl:text>
		<xsl:apply-templates select="@id"/>
		<xsl:text>children:[</xsl:text>
		<!-- Note that GFX does not yet support fills etc. on a group, even though SVG does. -->
		<!-- It's a planned enhancement though, so when GFX gets the ability to handle these, -->
		<!-- remove the following ten lines and stop propagating groupAttrs. -->
		<xsl:variable name="newGroupAttrs">
			<xsl:value-of select="$groupAttrs"/>
			<xsl:apply-templates select="@style"/>
			<!-- Note that we make no effort to guard against overlapping styles; we just order -->
			<!-- them to be consistent.  This naive approach will usually, but not always, work. -->
			<xsl:apply-templates select="@fill"/>
			<xsl:call-template name="stroke">
				<xsl:with-param name="node" select="."/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:apply-templates select="&SupportedElements;">
			<xsl:with-param name="groupAttrs" select="$newGroupAttrs"/>
		</xsl:apply-templates>
		<xsl:text>]</xsl:text>
		<xsl:if test="not(position()=last())"> 
			<xsl:text >,</xsl:text> 
		</xsl:if>
		<!-- When GFX gets group fills etc., remove the following line and uncomment the ones below. -->
		<xsl:apply-templates select="@transform"/>
		<!--<xsl:call-template name="common-attributes">-->
		<!--	<xsl:with-param name="node" select="."/>-->
		<!--</xsl:call-template>-->
		<xsl:text>}</xsl:text>
		<xsl:if test="not(position()=last())"> 
			<xsl:text >,</xsl:text> 
		</xsl:if>
	</xsl:template>

	<xsl:template match="svg:image">
		<xsl:param name="groupAttrs" select="''"/>
		<!-- Note that images must be GIF, JPEG, or PNG. -->
		<xsl:if test="not(parent::pattern)">
			<!-- When being used as a background pattern we don't want type info. -->
			<xsl:text>{</xsl:text>
			<xsl:apply-templates select="@id"/>
			<xsl:text>shape:{type:"image",</xsl:text>
		</xsl:if>
		<xsl:apply-templates select="@x|@y|@width|@height"/>
		<xsl:text>src:"</xsl:text>
		<xsl:value-of select="@xlink:href"/>
		<xsl:text>",</xsl:text>
		<xsl:if test="not(parent::pattern)">
			<xsl:text>},</xsl:text>
			<xsl:value-of select="$groupAttrs"/>
			<xsl:call-template name="common-attributes">
				<xsl:with-param name="node" select="."/>
			</xsl:call-template>
			<xsl:text>},</xsl:text>
		</xsl:if>
	</xsl:template>

	<xsl:template match="svg:line">
		<xsl:param name="groupAttrs" select="''"/>
		<xsl:text>{</xsl:text>
		<xsl:apply-templates select="@id"/>
		<xsl:text>shape:{type:"line",</xsl:text>
		<xsl:apply-templates select="@x1|@y1|@x2|@y2"/>
		<xsl:text>},</xsl:text>
		<xsl:value-of select="$groupAttrs"/>
		<xsl:call-template name="common-attributes">
			<xsl:with-param name="node" select="."/>
		</xsl:call-template>
		<xsl:text>}</xsl:text>
		<xsl:if test="not(position()=last())"> 
			<xsl:text >,</xsl:text> 
		</xsl:if>
	</xsl:template>

	<xsl:template match="svg:linearGradient">
		<xsl:text>{type:"linear",</xsl:text>
		<!-- Kluge alert - GFX doesn't handle gradientTransforms. We can help in -->
		<!-- the common case of matrix transforms in user space. Other cases we ignore. -->
		<!-- Even for this one case the results aren't anywhere near as good as real support in GFX. -->
		<xsl:choose>
			<!-- Kluge alert - this code is only meant to serve until GFX gets gradientTransform support. -->
			<!-- Once GFX does gradientTransforms, only the straight apply-templates should be kept. -->
			<xsl:when test="starts-with(@gradientTransform,'matrix') and @gradientUnits='userSpaceOnUse'">
				<xsl:call-template name="gradient-adjuster">
					<xsl:with-param name="node" select="."/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="@x1|@x2|@y1|@y2"/>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text>colors:[</xsl:text>
		<xsl:apply-templates select="svg:stop"/>
		<!-- Unfortunately GFX doesn't do gradientTransforms. -->
		<!-- Uncommenting the following would support it here. -->
		<!-- <xsl:apply-templates select="@x1|@x2|@y1|@y2"/> -->
		<!-- <xsl:apply-templates select="@gradientTransform"/> -->
		<xsl:text>]}</xsl:text>
		<xsl:if test="not(position()=last())"> 
			<xsl:text >,</xsl:text> 
		</xsl:if>
	</xsl:template>

	<xsl:template match="svg:path">
		<xsl:param name="groupAttrs" select="''"/>
		<xsl:if test="not(parent::textpath)">
			<!-- When being used within a textpath we don't want type info. -->
			<xsl:text>{</xsl:text>
			<xsl:apply-templates select="@id"/>
			<xsl:text>shape:{type:"path",</xsl:text>
		</xsl:if>
		<xsl:apply-templates select="@d"/>
		<xsl:if test="not(parent::textpath)">
			<xsl:text>},</xsl:text>
			<xsl:value-of select="$groupAttrs"/>
			<xsl:call-template name="common-attributes">
				<xsl:with-param name="node" select="."/>
			</xsl:call-template>
			<xsl:text>},</xsl:text>
		</xsl:if>
	</xsl:template>

	<xsl:template match="svg:pattern">
		<!-- GFX only seems to handle image pattern type fills, so that's all we do -->
		<xsl:text>{type:"pattern",</xsl:text>
		<xsl:apply-templates select="@width|@height|@xlink:href"/>
		<xsl:text>}</xsl:text>
		<xsl:if test="not(position()=last())"> 
			<xsl:text >,</xsl:text> 
		</xsl:if>
	</xsl:template>

	<xsl:template match="svg:polygon|svg:polyline">
		<xsl:param name="groupAttrs" select="''"/>
		<!-- Polygons are mostly treated as polylines -->
		<xsl:text>{</xsl:text>
		<xsl:apply-templates select="@id"/>
		<xsl:text>shape:{type:"polyline",points:[</xsl:text>
		<!-- We just have to ensure that endpoints match for a polygon; it's assumed in SVG -->
		<xsl:variable name="seminormalizedPoints" select="normalize-space(@points)"/>
		<xsl:variable name="normalizedPoints">
			<xsl:call-template name="kill-extra-spaces">
				<xsl:with-param name="string" select="$seminormalizedPoints"/>
			</xsl:call-template>
		</xsl:variable>
		<xsl:variable name="firstPoint" select="substring-before($normalizedPoints,' ')"/>
		<xsl:choose>
			<xsl:when test="contains(local-name(),'polygon') and
				$firstPoint!=substring($normalizedPoints,string-length($normalizedPoints)-string-length($firstPoint)+1)">
				<xsl:call-template name="point-processor">
					<xsl:with-param name="points" select="concat($normalizedPoints,' ',$firstPoint)"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="point-processor">
					<xsl:with-param name="points" select="$normalizedPoints"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text>]},</xsl:text>
		<xsl:value-of select="$groupAttrs"/>
		<xsl:call-template name="common-attributes">
			<xsl:with-param name="node" select="."/>
		</xsl:call-template>
		<xsl:text>}</xsl:text>
		<xsl:if test="not(position()=last())"> 
			<xsl:text >,</xsl:text> 
		</xsl:if>
	</xsl:template>

	<xsl:template match="svg:radialGradient">
		<xsl:text>{type:"radial",</xsl:text>
		<!-- Kluge alert - GFX doesn't handle gradientTransforms. We can help in -->
		<!-- the common case of matrix transforms in user space. Other cases we ignore. -->
		<!-- Even for this one case the results aren't anywhere near as good as real support in GFX. -->
		<xsl:choose>
			<!-- Kluge alert - this code is only meant to serve until GFX gets gradientTransform support. -->
			<!-- Once GFX does gradientTransforms, only the straight apply-templates should be kept. -->
			<xsl:when test="starts-with(@gradientTransform,'matrix') and @gradientUnits='userSpaceOnUse'">
				<xsl:call-template name="gradient-adjuster">
					<xsl:with-param name="node" select="."/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="@cx|@cy|@r"/>
			</xsl:otherwise>
		</xsl:choose>
		<!-- GFX doesn't currently support fx & fy -->
		<!-- Uncommenting the following would support it here. -->
		<!-- <xsl:apply-templates select="@fx|@fy"/> -->
		<xsl:text>colors:[</xsl:text>
		<xsl:apply-templates select="svg:stop"/>
		<!-- Unfortunately GFX doesn't do gradientTransforms. -->
		<!-- Uncommenting the following would support it here. -->
		<!-- <xsl:apply-templates select="@cx|@cy|@r"/> -->
		<!-- <xsl:apply-templates select="@gradientTransform"/> -->
		<xsl:text>]}</xsl:text>
		<xsl:if test="not(position()=last())"> 
			<xsl:text >,</xsl:text> 
		</xsl:if>
	</xsl:template>

	<xsl:template match="svg:rect">
		<xsl:param name="groupAttrs" select="''"/>
		<xsl:text>{</xsl:text>
		<xsl:apply-templates select="@id"/>
		<xsl:text>shape:{type:"rect",</xsl:text>
		<xsl:apply-templates select="@x|@y|@width|@height"/>
		<xsl:if test="@rx and @ry">
			<!-- Do approximate rounded corners if both an rx and ry are present. -->
			<xsl:variable name="r" select="(@rx+@ry) div 2"/>
			<xsl:text>r:</xsl:text>
			<xsl:value-of select="$r"/>
		</xsl:if>
		<xsl:text>},</xsl:text>
		<xsl:value-of select="$groupAttrs"/>
		<xsl:call-template name="common-attributes">
			<xsl:with-param name="node" select="."/>
		</xsl:call-template>
		<xsl:text>}</xsl:text>
		<xsl:if test="not(position()=last())"> 
			<xsl:text >,</xsl:text> 
		</xsl:if>
	</xsl:template>

	<xsl:template match="svg:stop">
		<!-- Both gradient types use the same sort of stops -->
		<xsl:text>{</xsl:text>
		<xsl:apply-templates select="@offset"/>
		<xsl:text>color:</xsl:text>
		<xsl:apply-templates select="@style"/>
		<xsl:text>}</xsl:text>
		<xsl:if test="not(position()=last())"> 
			<xsl:text >,</xsl:text> 
		</xsl:if>
	</xsl:template>

	<xsl:template match="svg:text|svg:textPath">
		<xsl:param name="groupAttrs" select="''"/>
		<!-- Support for textPath is not functional as GFX doesn't seem to have a -->
		<!-- complete serialized form at this time. %FIX% -->
		<xsl:text>{</xsl:text>
		<xsl:apply-templates select="@id"/>
		<xsl:choose>
			<xsl:when test="contains(local-name(),'textpath')">
				<xsl:text>shape:{type:"textpath",text:"</xsl:text>
				<xsl:apply-templates/>
				<xsl:text>",</xsl:text>
				<xsl:variable name="arguments" select="translate(normalize-space(substring-before(substring-after(@xlink:href,'('),')')),' ',',')"/>
				<xsl:call-template name="url-processor">
					<xsl:with-param name="url" select="$arguments"/>
				</xsl:call-template>
			</xsl:when>
			<xsl:otherwise>
				<!-- Regular text has slightly different attributes -->
				<xsl:choose>
					<!-- It's possible for a text element to contain a textpath element. -->
					<xsl:when test="not(textpath)">
						<xsl:text>shape:{type:"text",text:"</xsl:text>
						<xsl:apply-templates/>
						<xsl:text>",</xsl:text>
						<xsl:apply-templates select="@x|@y"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:apply-templates/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
		<xsl:text>},</xsl:text>
		<!-- Kluge alert - if no fill is defined, GFX won't display anything -->
		<!-- Our quick fix here is to force a fill of some sort. -->
		<xsl:if test="not(@fill)">
			<xsl:text>fill:"#000000",</xsl:text>
		</xsl:if>
		<xsl:value-of select="$groupAttrs"/>
		<xsl:call-template name="common-attributes">
			<xsl:with-param name="node" select="."/>
		</xsl:call-template>
		<xsl:text>}</xsl:text>
		<xsl:if test="not(position()=last())"> 
			<xsl:text >,</xsl:text> 
		</xsl:if>
	</xsl:template>
	
	<xsl:template match="svg:use">
		<xsl:param name="groupAttrs" select="''"/>
		<!-- Use just refers to an existing element, essentially duplicating it. -->
		<xsl:variable name="newGroupAttrs">
			<xsl:value-of select="$groupAttrs"/>
			<xsl:apply-templates select="@style"/>
			<!-- Note that we make no effort to guard against overlapping styles; we just order -->
			<!-- them to be consistent.  This naive approach will usually, but not always, work. -->
			<xsl:apply-templates select="@fill"/>
			<xsl:call-template name="stroke">
				<xsl:with-param name="node" select="."/>
			</xsl:call-template>
			<xsl:apply-templates select="@transform"/>
		</xsl:variable>
		<xsl:call-template name="url-processor">
			<xsl:with-param name="url" select="normalize-space(@xlink:href)"/>
			<xsl:with-param name="groupAttrs" select="$newGroupAttrs"/>
		</xsl:call-template>
	</xsl:template>

	<!-- The main SVG element itself -->
	
	<xsl:template match="/svg:svg">
		<xsl:text>[</xsl:text>
		<xsl:apply-templates select="&SupportedElements;"/>
		<xsl:text>]</xsl:text>
	</xsl:template>
</xsl:stylesheet>