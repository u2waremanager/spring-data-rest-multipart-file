export default [
    {
        path: '/:backend',
        name: 'Multipart',
        meta: { title: ''},
        component: () => import(/* webpackChunkName: "Multipart" */ '../apps/Multipart.vue'), 
    }
]
