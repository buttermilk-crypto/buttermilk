## DSA Key Example

Here's what a 2048 bit key looks like in the formatted condition. Source code:

	char [] pass = {'p','a','s','s'};
	DSAKeyContents contents = CryptoFactory.INSTANCE.generateKeys(pass);
	Assert.assertNotNull(contents);
	DSAKeyForPublication pub = contents.cloneForPublication();
	JSONFormatter formatter = new JSONFormatter("Chinese Eyes", "dave@cryptoregistry.com");
	formatter.add(pub);
	formatter.add(contents);
	StringWriter writer = new StringWriter();
	formatter.format(writer);
	System.err.println(writer.toString());

Here's the output:

	{
	  "Version" : "Buttermilk Key Materials 1.0",
	  "RegHandle" : "Chinese Eyes",
	  "Email" : "dave@cryptoregistry.com",
	  "Keys" : {
	    "fe29d552-821c-4048-bfee-8bb42ed78299-P" : {
	      "KeyAlgorithm" : "DSA",
	      "CreatedOn" : "2016-04-09T12:43:52.735+10:00",
	      "Encoding" : "Base64url",
	      "Strength" : "2048",
	      "P" : "APXSwdkHHgKA9Z0v_Cn_8hdlTJJqbewq3lovxMWJqDgpKArB12-LBSPmoTpQy7pfVC9-NHFxBmdSMgPyoMvj6c4nGBmgB8lPefn9vvrTgGl0OslXmWyK5TOSoTa4_3uw5LI7HDHu5pf7DcZ96F2uN4IJXoI862DyMjyq2LPuYGGnFn4s8iW6ZkNqvVsrY34vdlBUx5LMIiQTN8nQMph-QUxDI3GMCAxmC9RdiXcyyOru-vHpQUex23aifrB9oPc6xafo_lui9VJMmZkAbOlqDHifCrGT0e6hz_NjbMTqg4lypTSBbIYQ99V3a3FveQJaZgTryuWEeau0q4lbww18YUE=",
	      "Q" : "ANRLDrAlKqaaSd_NFCTCfj_-vU_h",
	      "G" : "cCOn3LrDnnyn8mnPXDoaiEzj6xvk_AojLIaSG8kv9hjIl-gSbkELsdp1Jp1Q1tjT4Mr6YP0tT7OOF0kFot9xHbs1D-3O8XBJQ3f64gVjjneG8e1EC8G2WJhJRuTVpRLsUEU8JIi5jjCDjr6qJ_fSGw0hHxQ5MxgPB3okK1-W80xQEJUtWRmtPB-JadtO7dHV6XIOM6HjTttT1VVRRs3LSIWCvZGg-8dUFD4ouIj1UpVamKOCNqxbwgJMQLCRmamxjGfp3HVq_a1JRr0KaLCE3QnQxEB8UXA9LqnRJ6YAGmkYdsaA3a9xYfxEG7lmA85L5CEr29xBBwkYFF-YWwLXDg==",
	      "Y" : "AOWheMtaNIfWToIXHfM0OoUq142ZdrfPkwA_8WLZU7lyikHNgsh5UFSc5oSkC7jhGBRUMG4SeHumbQL3InrHVBKNIkBO0m49xNwb9zjXJlq-7U3-zghH5TVjcUXKPD2UPt1SE9fpdIK54qrpOwPBRqAy6klhpfHvxmr_5IgA6TyeDLQusUlphYgTJrsrQ73DPCnOOJ9KtJm33Zx9iPI6F0zT2oxzXLgvMjNCv7ilP9IrI2Qxz7EzbMDTFSLaJg0c4OomjJsMZY_enzQlyVUcSKGje8wysar5wn4LqlZuwgUdFankdqJKlyWgf6GfMOnI2-VXJIncZXHPaaQ44a2bImw="
	    },
	    "fe29d552-821c-4048-bfee-8bb42ed78299-S" : {
	      "KeyData.Type" : "DSA",
	      "KeyData.Strength" : "2048",
	      "KeyData.PBEAlgorithm" : "PBKDF2",
	      "KeyData.EncryptedData" : [
	        "iABCwP-zdPnP8O3_C-rHzboSrFneQa71g0Cw5iZqJ6JUROKPOOfIo1HAg4y38L0BbrL9HVXc",
	        "kFn29LhyRg0-GzfYzzAnGPwFq_MT9_ttL85zG-SKbAe_rGodpOOkeE5ExsSyjIKehrucqngK",
	        "svQ_Z7tDSNrwz-xxuauSWs26D2LVoVwpAbCL7xr0tkNG1QI_Wp7ZIXztVNC3kw5p6LZXeAqd",
	        "E2y8A-fSgnOqLCvBrFQGgY66BtKZAUy1efQKy-Xe45E-hw0QykdMhno59aTM-DXv5labrrXJ",
	        "LlF_P26Lgo2qqTg_rB9muXFbHhlFrbevrt3LLABIKiJER2_80pcjEdY_Yfw8j72yJ0qRiPhj",
	        "plwHYFPTGi7NVSfj4ojxOYTS5ZI6rjfe2L8YtSr4fytREWZObXK96MUy6Z0SuqFLrqsGt-o5",
	        "nk4S1oy3zLxNo84EsnwtHeH2E2WJmoqo_HyV61viCfFQixxXnEbVa8bnz2Box7lneB38ieDY",
	        "H8e94C1_dUaCa-8iAJYzzf6QD_jpQdQz707d54uRnHI_gKXpSIYi2rSs5urabqmx1MfURAtA",
	        "kHk9iyn-Yzu0R50lLTRi8HTq0Qb96qy_yGbFF5ihah2p3D4zhhoXd3nLIxs5XTYxXxDgOqWA",
	        "bWFS7yMZaDfgcfSgrutp66xMu9RIEYWJJCwyqS8GgqYjiou-as3FBx1g1IUonAkzeuPyK-gJ",
	        "2eIE-iddfKgppeyJV1VSFuhe-hHqELVDDQkt8u1OmtP10_pBidot3Z705a8kHUMLzmCnlXos",
	        "R5I9X-0cz4rsVzjyMQXMVNhHup4vXdiaXqsr6o1Fja5l4iUiCm5s0HYLAS7ESboesN6kYK0g",
	        "JKtz6Tuji59gc0ire-Nwv6f4JJCCJE-6ol3QwSMi5gCuqvw9Fn6pHR7ZItSl5r4zONpxYZ2I",
	        "Gi63OomFFJ44qmvxgmQo3YcA2Cu4L58_Vtwqix8mgg78xT3d1kf3r4WRXtChNmO7QBio0GkC",
	        "CX_OMaTFKYRtDMAIBImnXelKGKbcViGew7xJ1NWdsmR5HO814gtM1bINNz7y6fhJdkC-h5iL",
	        "QeBz19XigXWdG1cLseU4PTYBiJ7QrIltfTdQICAYwv3OhPniNzd6iE73K4BMO82t-Dj-ypzP",
	        "s7OswKXVl2zv-YwkNT6N7ywfd3eFYq7eXQShtdmzdNiQvBLwH-NsIjbOkWVgAYJxbqyeuGvX",
	        "HdMFveWfhLT4hpWNtCynloGAVkXr6xvPISxn2qZjCai4uDRSqX2pzC7ukUubucu9XBY1j9ji",
	        "ivGFzrJ7vp93kGpL2L28IYt5PAlsIvJW_vEfSG_G-bh_CWi1li2JCGc0dqTO3wchXAaPNAA6",
	        "Sn_yDreSDO29W6UHOAkddx9NkEN-73ZerOz1FNH1kUv2V34lJWX8rRTCOwrGG-iChgVeLx-E",
	        "tCkdLrsM3d4iOOvCoE-dVNEAgCt-PV9CgpIJaAM98JJa-E8AxMn1ZUvsvX4vSpk1TJN5hbR_",
	        "J5LXFHlOftY5DhNs9d1pjuGbiFcW7Bh3cgLW5zNhNPP5xudTN0hJC6lshoGI3q21BpMCcQ9_",
	        "Wy0VQjbNw_iUulHv5zI48NmafptLUepr3RrbzOk6ovAYr9kJcs4LEhGe7ammNcXveCmiK70x",
	        "Qd1SCyQvVtZxjlU4--b80eJ5DHnp2OSlVGJ6EALGcHJx2Nbih5M7dqxVvlPimnAH2lGyZ_Z9",
	        "6IOZKS5O0ge5LgcfDeroslaXcc52lBLE8LlRmrQ6hMV1ibYfsSNwmzS0YYlvvZml3l5zZM7d",
	        "pP6eLS02z52UT6n7f2XGqsJOwQln287JLDp-pPw_Ag75tMtWJ4XNf1nA6PguuLnKxV8nynxJ",
	        "krQkXd5l6i_zdYFhka-Jjwh1TQlCC1WYnPr4fhvF2l4XSstCXZPYYUlG0sqPrLEBmtUeSw==",
	        ""
	      ],
	      "KeyData.PBESalt" : "T0bSIGVBJfzUE3nXTRcR-LKuj-p_o7xg0hMzDWfO0_I=",
	      "KeyData.Iterations" : "10000"
	    }
	  }
	}
	
