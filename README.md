# http4sftw

I want to create a service, that receives multiple images being sent as a multipart request via POST.

My code is heavily influenced by the excellent example of @gvolpe here: https://github.com/gvolpe/advanced-http4s/blob/master/src/main/scala/com/github/gvolpe/http4s/server/endpoints/MultipartHttpEndpoint.scala 

The request is decoded with a `EntityDecoder.multipart` where all the parts are being extracted successfully.
To store the images the `Vector[Part[IO]]` is being traversed to store the files.
However - this doesn't work when I send more than one file to the server -- only the first one is being processed.
When I process the parts manually in a Stream it works with all files. 

But in neither of both cases does the client receive the Ok-message.
I created two routes (one with traverse and one without).
See `de.flwi.http4sMultipartTraverseProblem.service.FileUploadService` for details.

## catch me if you can 
Run either 
- the testcases with `sbt test` 
- or start the server with `sbt run` and try to upload to both endpoints via cURL with these statements. 


```bash
IMAGES="src/test/resources/example-images"

curl -v -F "1.png=@$IMAGES/1.png" -F "2.png=@$IMAGES/2.png" -F "3.png=@$IMAGES/3.png" http://localhost:8080/upload-images-without-traverse
# or
curl -v -F "1.png=@$IMAGES/1.png" -F "2.png=@$IMAGES/2.png" -F "3.png=@$IMAGES/3.png" http://localhost:8080/upload-images-with-traverse
    
```