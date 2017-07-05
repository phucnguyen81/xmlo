# Description
Utilities for building strings, xml and xslt.

# Examples
See test cases for all examples.

## Build xml string
The following code:

```xml
Str company = new Str() {
    @Override
    protected void doBuild() {
        l("<company>");
        l(L, "<staff id=\"1\">");
        l(L, L, "<firstname>Phuc</firstname>");
        l(L, L, "<lastname>Nguyen</lastname>");
        l(L, L, "<salary>100000</salary>");
        l(L, "</staff>");
        l("</company>");
    }
}.indent("  ").build();
```

creates a Str object for the string:

```xml
<company>
  <staff id="1">
    <firstname>Phuc</firstname>
    <lastname>Nguyen</lastname>
    <salary>100000</salary>
  </staff>
</company>
```

## Build xml Node
The following code:

```java
Node orders = new Xml() {
    @Override
    protected void doBuild() {
        ae("orders");
        ae(L, c("good choice"));
        ae(L, e("order", id(553)));
        ae(L, L, e("amount", t("$45.00")));
    }
}.build();
```

creates a Node object for xml:

```xml
<orders>
    <!--good choice-->
    <order id="553">
        <amount>$45.00</amount>
    </order>
</orders>
```

## Build xsl Node
The following code:

```java
Xsl xsl = new Xsl() {
    @Override
    protected void doBuild() {
        ae(xslStyleSheetV1());
        ae(L, xsOutputXml(noindent(), omitXmlDecl()));

        ae(L, xsTemplate("eu"));
        ae(L, L, xsApply("member"));

        ae(L, xsTemplate("member"));
        ae(L, L, "eu-members");
        ae(L, L, L, xsApply("state[@founding]"));

        ae(L, xsTemplate("state"));
        ae(L, L, xsCopy());
        ae(L, L, L, xsApply());
    }
}.build();
```

creates a Node object for xslt:

```xml
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
 <xsl:output method="xml" indent="no" omit-xml-declaration="yes" />
   
 <xsl:template match="eu">
  <xsl:apply-templates select="member"/>
 </xsl:template>
   
 <xsl:template match="member">
 <eu-members>
   <xsl:apply-templates select="state[@founding]"/>
 </eu-members>
 </xsl:template>
   
 <xsl:template match="state">
  <xsl:copy>
   <xsl:apply-templates/>
  </xsl:copy>
 </xsl:template>
   
</xsl:stylesheet>
```

# License
No conditions whatsoever, see UNLICENSE.txt for details.