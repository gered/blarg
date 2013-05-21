{
   "_id": "_design/files",
   "language": "javascript",
   "views": {
       "listAll": {
           "map": "function(doc) {\n  emit(doc._id, doc);\n}"
       },
       "listPublished": {
           "map": "function(doc) {\n  if (doc.published === true)\n    emit(doc._id, doc);\n}"
       }
   }
}