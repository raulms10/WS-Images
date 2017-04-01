private CustomShapeImageView mImageView;
compile 'com.mostafagazar:customshapeimageview:1.0.4'

public void getImage(String url) {

        Log.d(TAG, "Getting Image");

        if (url != null) {
            // Retrieves an image specified by the URL, displays it in the UI.
            ImageRequest request = new ImageRequest(url,
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap bitmap) {

                            mBitmap = bitmap;
                            mImageView.setImageBitmap(bitmap);
                            mProgressDialog.dismiss();
                            Log.d(TAG, "onResponse: ok");
                        }
                    }, 0, 0, null,
                    new Response.ErrorListener() {
                        public void onErrorResponse(VolleyError error) {
                            mImageView.setImageResource(R.drawable.defaultprofilepicture);
                            Log.d(TAG, "onErrorResponse: " + error.toString());
                            mProgressDialog.dismiss();

                            if (error.networkResponse != null && error.networkResponse.statusCode == 401) {
                                Utils.handleUnauthorized(EditActivity.this);
                            }
                        }
                    })

            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("SessionId", mSessionId);

                    return headers;
                }
            };
            VolleySingleton.getInstance(this).addToRequestQueue(request);
        } else
            mProgressDialog.dismiss();
    }



private void sendImage() {

        //String url = "https://www.fusemobiledevelopment.com/AlertZone/Services/api/v1/user/profilepicture";
        String url = ConfigURL.ProfilePicture;
        Log.d(TAG, "Attempt to send image");
        mProgressDialog.show();

        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
                int messageCode = 0;
                Log.d(TAG, "SendNumber response: " + response.toString());

                try {
                    JSONObject result = new JSONObject(resultResponse);
                    Log.d(TAG, "Response: " + result.toString());

                    messageCode = result.getInt("MessageCode");

                    if (messageCode == 2000) {

                        isSaved = true;
                        mProgressDialog.dismiss();

                        Log.d(TAG, "Image sent");
                        returnData();
                        finish();

                    } else {
                        Log.d(TAG, "attempt Send Number failed, messageCode: " + messageCode);
                        //showMessage(messageCode);
                        mProgressDialog.dismiss();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("onErrorResponse", "onErrorResponse: " + error.toString());
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
//                params.put("api_token", "gh659gjhvdyudo973823tt9gvjf7i6ric75r76");
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                params.put("image", new DataPart("name_picture.jpg", Utils.getByteArrayFromBitmap(mBitmap), "image/jpeg"));
                //params.put("cover", new DataPart("file_cover.jpg", AppHelper.getFileDataFromDrawable(getBaseContext(), mCoverImage.getDrawable()), "image/jpeg"));

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();

                headers.put("SessionId", mSessionId);

                return headers;
            }
        };
        VolleySingleton.getInstance(getBaseContext()).addToRequestQueue(multipartRequest);

    }
