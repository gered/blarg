{
   "_id": "_design/files",
   "language": "javascript",
   "views": {
       "listAll": {
           "map": "function(doc) {\n  var fullFilename = doc.path + '/' + doc.filename;\n  emit(fullFilename, doc);\n}"
       }
   }
}