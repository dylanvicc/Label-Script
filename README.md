# Label Script
A hand rolled domain-specific-language to bring human readable commands to the painful world of printer recognized languages.
Supports conditional checks, concatenation, and transpilation of intake variables. Free to use and to modify.

This application stands up as a middleware between a user and a printer. It then intakes a custom request with variables and label script code, as well as the target destination. Currently supports fire and forget to network and mobile printers.

# Example Script
```
label 800x600 dpi 203

text at (20,20) ${product.name}

if ${product.weight} > "5"
{
  text at (20,60) "HEAVY ITEM"
  barcode code128 at (20,100) ${product.id} height 80 human
}
else
{
  barcode code128 at (20,60) ${product.id} height 80 human
}
```

# Example Request
```
{
  "script": "...",
  "data": {
    "product": {
      "name": "Fishing Rod"
    }
  },
  "printer": {
    "type": "ZPL",
    "transport": "NETWORK",
    "target": "192.168.1.50"
  }
}
```
