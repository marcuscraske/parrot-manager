export function initApp() : () => Promise<any>
{
    return () : Promise<any> =>
    {
        return new Promise((resolve, reject) =>
        {
            var handle = setInterval(() =>
            {
                console.log("checking if runtime ready...");

                var win = (window as any);
                if (win.runtimeService != null && win.runtimeService.isReady() == true)
                {
                    console.log("runtime ready, bootstrapping app");

                    // stop interval
                    clearInterval(handle);

                    // continue app startup/init
                    resolve();
                }

            }, 100);
        });
    }
}
