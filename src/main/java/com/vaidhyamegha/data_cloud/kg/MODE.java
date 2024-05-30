package com.vaidhyamegha.data_cloud.kg;

enum MODE {BUILD("build"), CLI("cli"), SERVER("server");
    String name = "";

    MODE(String m) {
        if (m.equals("build") || m.equals("cli") || m.equals("server")  )
            name = m;
        else throw new RuntimeException("Unsupported mode");
    }
}
