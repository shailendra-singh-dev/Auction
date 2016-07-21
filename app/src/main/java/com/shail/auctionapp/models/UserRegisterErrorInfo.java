package com.shail.auctionapp.models;

import java.util.List;

public class UserRegisterErrorInfo
{
    /*
        {
            "error": {
                "name": "ValidationError",
                "status": 422,
                "message": "The `UserLogin` instance is not valid. Details: `email` Email already exists (value: \"fred@livinglarge.com\").",
                "statusCode": 422,
                "details": {
                    "context": "UserLogin",
                    "codes": {
                        "email": ["uniqueness"]
                    },
                    "messages": {
                        "email": ["Email already exists"]
                    }
                },
                "stack": "ValidationError: The `UserLogin` instance is not valid. Details: `email` Email already exists (value: \"fred@livinglarge.com").\n    at /root/server/-api/node_modules/loopback-datasource-juggler/lib/dao.js:265:12\n    at ModelConstructor.<anonymous> (/root/server/-api/node_modules/loopback-datasource-juggler/lib/validations.js:487:13)\n    at ModelConstructor.next (/root/server/-api/node_modules/loopback-datasource-juggler/lib/hooks.js:75:12)\n    at done (/root/server/-api/node_modules/loopback-datasource-juggler/lib/validations.js:484:25)\n    at /root/server/-api/node_modules/loopback-datasource-juggler/lib/validations.js:558:7\n    at ModelConstructor.<anonymous> (/root/server/-api/node_modules/loopback-datasource-juggler/lib/validations.js:357:5)\n    at /root/server/-api/node_modules/loopback-datasource-juggler/lib/dao.js:1452:9\n    at done (/root/server/-api/node_modules/loopback-datasource-juggler/node_modules/async/lib/async.js:167:19)\n    at /root/server/-api/node_modules/loopback-datasource-juggler/node_modules/async/lib/async.js:40:16\n    at /root/server/-api/node_modules/loopback-datasource-juggler/lib/dao.js:1438:13\n    at doNotify (/root/server/-api/node_modules/loopback-datasource-juggler/lib/observer.js:93:49)\n    at doNotify (/root/server/-api/node_modules/loopback-datasource-juggler/lib/observer.js:93:49)\n    at doNotify (/root/server/-api/node_modules/loopback-datasource-juggler/lib/observer.js:93:49)\n    at doNotify (/root/server/-api/node_modules/loopback-datasource-juggler/lib/observer.js:93:49)\n    at doNotify (/root/server/-api/node_modules/loopback-datasource-juggler/lib/observer.js:93:49)\n    at Function.ObserverMixin._notifyBaseObservers (/root/server/-api/node_modules/loopback-datasource-juggler/lib/observer.js:116:5)\n    at Function.ObserverMixin.notifyObserversOf (/root/server/-api/node_modules/loopback-datasource-juggler/lib/observer.js:91:8)"
            }
        }
    */
    public class UserRegisterErrorMessages
    {
        public List<String> email;
    }

    public class UserRegisterErrorDetails
    {
        public String                       context;
        public UserRegisterErrorMessages    messages;
    }

    public class UserRegisterError
    {
        public String   name;
        public String   message;
        public String   stack;
        public int      statusCode;

        public UserRegisterErrorDetails details;
    }

    public UserRegisterError error;
}
