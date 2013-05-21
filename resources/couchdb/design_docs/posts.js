{
   "_id": "_design/posts",
   "language": "javascript",
   "views": {
       "listTags": {
           "map": "function(doc) {\n  if (doc.type === \"post\") {\n    for (var i in doc.tags) {\n      emit(doc.tags[i], null);\n    }\n  }\n}",
           "reduce": "function(keys, values) {\n    return null;\n}"
       },
       "listPosts": {
           "map": "function(doc) {\n  if (doc.type === \"post\") {\n    emit([doc.created_at, doc.slug], doc)\n  }\n}"
       },
       "listPostsBySlug": {
           "map": "function(doc) {\n  if (doc.type === \"post\") {\n    var date = new Date(doc.created_at)\n    var dateStr = date.getFullYear() + \"-\" + (date.getMonth() + 1) + \"-\" + date.getDate()\n    emit([dateStr, doc.slug], doc)\n  }\n}"
       },
       "listPublishedPosts": {
           "map": "function(doc) {\n  if (doc.type === \"post\" && doc.published === true) {\n    emit([doc.created_at, doc.slug], doc)\n  }\n}"
       },
       "countPublishedPosts": {
           "map": "function(doc) {\n  if (doc.type === \"post\" && doc.published === true) {\n    emit(\"published post\", 1)\n  }\n}",
           "reduce": "function(keys, values) {\n  return sum(values);\n}"
       },
       "countPosts": {
           "map": "function(doc) {\n  if (doc.type === \"post\") {\n    emit(\"any post\", 1)\n  }\n}",
           "reduce": "function(keys, values) {\n  return sum(values);\n}"
       }
   }
}