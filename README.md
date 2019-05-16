# http4sftw

I want to create a service, that receives multiple images being sent as a multipart request via POST.

My code is heavily influenced by the excellent [example](https://github.com/gvolpe/advanced-http4s/blob/master/src/main/scala/com/github/gvolpe/http4s/server/endpoints/MultipartHttpEndpoint.scala) by @gvolpe.  
But this implementation had a bug which could be solved with the help of @jmcardon. See his explanation in [gitter](https://gitter.im/http4s/http4s?at=5cdc8c7d5a887e1cd9f455a9).

Run either 
- the testcase with `sbt test` 
- or start the server with `sbt run` and upload some example images to the endpoint via cURL. 


```bash
IMAGES="src/test/resources/example-images"

curl -v -F "1.png=@$IMAGES/1.png" -F "2.png=@$IMAGES/2.png" -F "3.png=@$IMAGES/3.png" http://localhost:8080/upload-images
```