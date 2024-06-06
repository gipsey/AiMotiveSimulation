#include <jni.h>
#include <fstream>
#include <string>

#include "log.h"

using namespace std;

static const char *FILE_NAME = "/data/data/aimotive.simulation/locations.txt";

extern "C" JNIEXPORT void JNICALL
Java_aimotive_simulation_data_FilePersistenceRepository_persistStartOfSession(
        JNIEnv *env,
        jobject /* this */,
        jstring dateTime) {
    const char *dateTimeString = env->GetStringUTFChars(dateTime, NULL);
    LOGD("persistStartOfSession - going to persist '%s'", dateTimeString);

    ofstream locationsFile;
    locationsFile.open(FILE_NAME, fstream::app);
    locationsFile << dateTimeString << "\n";
    locationsFile.close();
}

extern "C" JNIEXPORT void JNICALL
Java_aimotive_simulation_data_FilePersistenceRepository_persist(
        JNIEnv *env,
        jobject /* this */,
        jdouble latitude,
        jdouble longitude) {
    LOGD("persist - data to persist is latitude=%.7f, longitude=%.7f", latitude, longitude);

    char locationText[100];
    sprintf(locationText, "latitude=%.7f,longitude=%.7f", latitude, longitude);
    LOGD("persist - going to persist '%s' to '%s'", locationText, FILE_NAME);

    ofstream locationsFile;
    locationsFile.open(FILE_NAME, fstream::app);
    locationsFile << locationText << "\n";
    locationsFile.close();
}
