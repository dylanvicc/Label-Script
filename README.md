# Label Script
A domain specific language that transpiles human readable commands into printer recognized languages.

# Example
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

# Request
```
{
  "script": "...",
  "data": {
    "shipment": {
      "service": "OVERNIGHT",
      "trackingNumber": "1Z999AA10123456784",
      "fragile": "true",
      "date": "2026-05-24"
    },
    "recipient": {
      "name": "Acme Corporation",
      "address": "123 Main St",
      "city": "Dallas TX 75201"
    }
  },
  "printer": {
    "type": "ZPL",
    "transport": "NETWORK",
    "target": "192.168.1.50"
  }
}
```
